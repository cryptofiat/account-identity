package com.kryptoeuro.accountmapper.rest;

import com.codeborne.security.mobileid.MobileIDSession;
import com.kryptoeuro.accountmapper.command.AuthenticateCommand;
import com.kryptoeuro.accountmapper.command.BankTransferBasedAccountRegistrationCommand;
import com.kryptoeuro.accountmapper.command.PollCommand;
import com.kryptoeuro.accountmapper.domain.AuthorisationType;
import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.domain.PendingAuthorisation;
import com.kryptoeuro.accountmapper.response.AccountsResponse;
import com.kryptoeuro.accountmapper.response.AuthenticateResponse;
import com.kryptoeuro.accountmapper.response.AccountActivationResponse;
import com.kryptoeuro.accountmapper.service.AccountManagementService;
import com.kryptoeuro.accountmapper.service.EthereumService;
import com.kryptoeuro.accountmapper.service.MobileIdAuthService;
import com.kryptoeuro.accountmapper.service.PendingAuthorisationService;
import com.kryptoeuro.accountmapper.state.AuthenticationStatus;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Base64;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
@Slf4j
public class AccountMapperController {
	@Autowired
	MobileIdAuthService mobileIdAuthService;
	@Autowired
	EthereumService ethereumService;
	@Autowired
	AccountManagementService accountManagementService;
	@Autowired
	PendingAuthorisationService pendingAuthorisationService;

	private static boolean accountActivationEnabled = false;

	@ApiOperation(value = "Initiate mobile-id authorisation")
	@RequestMapping(
			method = POST,
			value = "/authorisations",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateCommand authenticateCommand) {
		// start mobile id auth

		PendingAuthorisation pendingAuthorisation = null;

		//Mobile ID
		if (authenticateCommand.getPhoneNumber() != null) {
			MobileIDSession mobileIDSession = mobileIdAuthService.startLogin(authenticateCommand.getPhoneNumber());
			pendingAuthorisation = pendingAuthorisationService.store(authenticateCommand.getAccountAddress(), mobileIDSession);
		} //Bank transfer
		else {
			pendingAuthorisation = pendingAuthorisationService.store(authenticateCommand.getAccountAddress());
		}

		return new ResponseEntity<AuthenticateResponse>(AuthenticateResponse.fromPendingAuthorisation(pendingAuthorisation), HttpStatus.OK);
	}

	@ApiOperation(value = "[Mobile ID polling endpoint] Validate authorisation, store new account-identity mapping and activate ethereum account")
	@RequestMapping(
			method = POST,
			value = "/accounts",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AccountActivationResponse> authorizeMobileIdAndCreateAccountIdentityMapping(@Valid @RequestBody PollCommand pollCommand) {
		PendingAuthorisation pendingAuthorisation = pendingAuthorisationService.findByAuthIdentifier(pollCommand.getAuthIdentifier());

		if (pendingAuthorisation == null) {
			return new ResponseEntity<AccountActivationResponse>(new AccountActivationResponse(AuthenticationStatus.LOGIN_EXPIRED, AuthorisationType.MOBILE_ID), HttpStatus.OK);
		}

		MobileIDSession mobileIDSession = MobileIDSession.fromString(pendingAuthorisation.getSerialisedMobileIdSession());
		String accountAddress = pendingAuthorisation.getAddress();

		if (mobileIDSession == null || accountAddress == null) {
			return new ResponseEntity<AccountActivationResponse>(new AccountActivationResponse(AuthenticationStatus.LOGIN_EXPIRED, AuthorisationType.MOBILE_ID), HttpStatus.OK);
		}

		// Check if authenticated
		if (mobileIdAuthService.isLoginComplete(mobileIDSession)) {
			pendingAuthorisationService.expire(pendingAuthorisation);
		} else {
			return new ResponseEntity<AccountActivationResponse>(new AccountActivationResponse(AuthenticationStatus.LOGIN_PENDING, AuthorisationType.MOBILE_ID), HttpStatus.OK);
		}

		EthereumAccount newAccount;
		try {
			newAccount = accountManagementService.storeNewAccount(accountAddress, mobileIDSession.personalCode, AuthorisationType.MOBILE_ID);

			if (accountActivationEnabled) {
				ethereumService.activateEthereumAccount(accountAddress);
			}
		} catch (Exception e) {
            log.error("Login failure", e);
			return new ResponseEntity<AccountActivationResponse>(new AccountActivationResponse(AuthenticationStatus.LOGIN_FAILURE, AuthorisationType.MOBILE_ID), HttpStatus.OK);
		}

		accountManagementService.markActivated(newAccount);

		return new ResponseEntity<AccountActivationResponse>(new AccountActivationResponse(AuthenticationStatus.LOGIN_SUCCESS, AuthorisationType.MOBILE_ID), HttpStatus.OK);
	}

	@ApiOperation(value = "[Prototype for Bank Transfer based account registration] Validate signed authIdentifier, store new account-identity mapping and activate ethereum account [BASIC AUTH]")
	@RequestMapping(
			method = PUT,
			value = "/accounts",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AccountActivationResponse> authorizeBankTransferAndCreateAccountIdentityMapping(@Valid @RequestBody BankTransferBasedAccountRegistrationCommand cmd,
																										  @RequestHeader(value = "Authorization") String authorization) {
		//Wow, such authorization, much secure
		try {
			if (authorization == null || !ethereumService.getParityAuthCredentials().equals(new String(Base64.getDecoder().decode(authorization.replace("Basic ", "")))))
				throw new Exception("Account registration via bank unauthorized");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage());
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		PendingAuthorisation pendingAuthorisation = pendingAuthorisationService.findByAuthIdentifier(cmd.getAuthIdentifier());
		if (pendingAuthorisation == null) {
			return new ResponseEntity<AccountActivationResponse>(new AccountActivationResponse(AuthenticationStatus.LOGIN_EXPIRED, AuthorisationType.BANK_TRANSFER), HttpStatus.OK);
		}

		EthereumAccount newAccount = accountManagementService.storeNewAccount(pendingAuthorisation.getAddress(), cmd.getOwnerId(), AuthorisationType.BANK_TRANSFER);
		if (accountActivationEnabled) {
//			ethereumService.activateEthereumAccount(newAccount.getAddress());
		}
		accountManagementService.markActivated(newAccount);
		pendingAuthorisationService.expire(pendingAuthorisation);

		return new ResponseEntity<AccountActivationResponse>(new AccountActivationResponse(AuthenticationStatus.LOGIN_SUCCESS, AuthorisationType.BANK_TRANSFER), HttpStatus.OK);
	}


	@ApiOperation(value = "View existing accounts")
	@RequestMapping(method = GET, value = "/accounts")
	public ResponseEntity<AccountsResponse> listAccounts(@RequestParam(name = "ownerId", required = false) String ownerId) {
		if (ownerId != null) {
			return new ResponseEntity<AccountsResponse>(AccountsResponse.fromEthereumAccounts(accountManagementService.getAccountsByOwnerId(ownerId)), HttpStatus.OK);
		}
		return new ResponseEntity<AccountsResponse>(AccountsResponse.fromEthereumAccounts(accountManagementService.getAllAccounts()), HttpStatus.OK);
	}

	@ApiOperation(value = "Remove account identity mapping")
	@RequestMapping(method = DELETE, value = "/accounts")
	public ResponseEntity<AccountsResponse> removeAccount(@RequestParam(name = "mappingId", required = true) Long mappingId) {
		accountManagementService.removeAccountById(mappingId);
		return new ResponseEntity<AccountsResponse>(AccountsResponse.fromEthereumAccounts(accountManagementService.getAllAccounts()), HttpStatus.OK);
	}
}
