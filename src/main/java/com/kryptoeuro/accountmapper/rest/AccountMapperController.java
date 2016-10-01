package com.kryptoeuro.accountmapper.rest;

import com.codeborne.security.mobileid.MobileIDSession;
import com.kryptoeuro.accountmapper.command.AuthenticateCommand;
import com.kryptoeuro.accountmapper.command.PollCommand;
import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.domain.PendingMobileIdAuthorisation;
import com.kryptoeuro.accountmapper.response.AuthenticateResponse;
import com.kryptoeuro.accountmapper.response.PollResponse;
import com.kryptoeuro.accountmapper.service.AccountManagementService;
import com.kryptoeuro.accountmapper.service.EthereumService;
import com.kryptoeuro.accountmapper.service.MobileIdAuthService;
import com.kryptoeuro.accountmapper.state.AuthenticationStatus;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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

	private static boolean accountActivationEnabled = true;

	//Initial HttpSession approach did not work with marat's app. Will keep in memory here for now
	private static Map<String, PendingMobileIdAuthorisation> pendingAuthorisations = new HashMap<String, PendingMobileIdAuthorisation>();

	@ApiOperation(value = "Initiate mobile-id authorisation")
	@RequestMapping(
			method = POST,
			value = "/authorisations",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateCommand authenticateCommand) {
		// start mobile id auth
		MobileIDSession mobileIDSession = mobileIdAuthService.startLogin(authenticateCommand.getPhoneNumber());

		pendingAuthorisations.put(mobileIDSession.challenge, new PendingMobileIdAuthorisation(mobileIDSession, authenticateCommand.getAccountAddress()));

		AuthenticateResponse authenticateResponse = new AuthenticateResponse(mobileIDSession.challenge, mobileIDSession.challenge); //todo change second parameter to unique identifier
		return new ResponseEntity<AuthenticateResponse>(authenticateResponse, HttpStatus.OK);
	}

	@ApiOperation(value = "[Polling endpoint] Validate authorisation, store new account-identity mapping and activate ethereum account")
	@RequestMapping(
			method = POST,
			value = "/accounts",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<PollResponse> authorizeAndCreateAccountIdentityMapping(@Valid @RequestBody PollCommand pollCommand) {
		PendingMobileIdAuthorisation pendingMobileIdAuthorisation = pendingAuthorisations.get(pollCommand.getAuthIdentifier());

		if (pendingMobileIdAuthorisation == null) {
			return new ResponseEntity<PollResponse>(new PollResponse(AuthenticationStatus.LOGIN_EXPIRED), HttpStatus.OK);
		}

		MobileIDSession mobileIDSession = pendingMobileIdAuthorisation.mobileIdSession;
		String accountAddress = pendingMobileIdAuthorisation.address;

		if (mobileIDSession == null || accountAddress == null) {
			return new ResponseEntity<PollResponse>(new PollResponse(AuthenticationStatus.LOGIN_EXPIRED), HttpStatus.OK);
		}

		// Check if authenticated
		boolean isAuthenticated = mobileIdAuthService.isLoginComplete(mobileIDSession);
		if (!isAuthenticated) {
			return new ResponseEntity<PollResponse>(new PollResponse(AuthenticationStatus.LOGIN_PENDING), HttpStatus.OK);
		}

		try {
			accountManagementService.storeNewAccount(accountAddress, mobileIDSession.personalCode);
			pendingMobileIdAuthorisation.mobileIdSession = null;

			if (accountActivationEnabled) {
				ethereumService.activateEthereumAccount(accountAddress);
			}

			pendingMobileIdAuthorisation.address = null;
		} catch (Exception e) {
            log.error("Login failure", e);
			return new ResponseEntity<PollResponse>(new PollResponse(AuthenticationStatus.LOGIN_FAILURE), HttpStatus.OK);
		}

		return new ResponseEntity<PollResponse>(new PollResponse(AuthenticationStatus.LOGIN_SUCCESS), HttpStatus.OK);
	}

	@ApiOperation(value = "View existing accounts")
	@RequestMapping(method = GET, value = "/accounts")
	public List<EthereumAccount> listAccounts(@RequestParam(name = "ownerId", required = false) String ownerId) {
		if (ownerId != null) {
			accountManagementService.getAccountsByOwnerId(ownerId);
		}
		return accountManagementService.getAllAccounts();
	}

	@ApiOperation(value = "Remove account identity mapping")
	@RequestMapping(method = DELETE, value = "/accounts")
	public List<EthereumAccount> removeAccount(@RequestParam(name = "mappingId", required = true) Long mappingId) {
		accountManagementService.removeAccountById(mappingId);
		return accountManagementService.getAllAccounts();
	}
}
