package eu.cryptoeuro.accountmapper.rest;

import com.codeborne.security.mobileid.MobileIDSession;
import eu.cryptoeuro.accountmapper.command.AuthenticateCommand;
import eu.cryptoeuro.accountmapper.command.BankTransferBasedAccountRegistrationCommand;
import eu.cryptoeuro.accountmapper.command.GetPaymentReferenceCommand;
import eu.cryptoeuro.accountmapper.command.PollCommand;
import eu.cryptoeuro.accountmapper.domain.AuthorisationType;
import eu.cryptoeuro.accountmapper.domain.EthereumAccount;
import eu.cryptoeuro.accountmapper.domain.PendingAuthorisation;
import eu.cryptoeuro.accountmapper.response.EscrowTransfer;
import eu.cryptoeuro.accountmapper.response.AccountsResponse;
import eu.cryptoeuro.accountmapper.response.AuthenticateResponse;
import eu.cryptoeuro.accountmapper.response.AccountActivationResponse;
import eu.cryptoeuro.accountmapper.response.PaymentReferenceResponse;
import eu.cryptoeuro.accountmapper.service.*;
import eu.cryptoeuro.accountmapper.state.AuthenticationStatus;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.spongycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import org.json.JSONException;
import java.security.Principal;
import java.util.Base64;
import java.util.List;

import static java.util.Collections.emptyList;
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
	EscrowService escrowService;
	@Autowired
	AccountManagementService accountManagementService;
	@Autowired
	PendingAuthorisationService pendingAuthorisationService;
	@Autowired
	WalletServerService walletService;

	private static boolean accountActivationEnabled = true;

	@ApiOperation(value = "Initiate authorisation")
	@RequestMapping(
			method = POST,
			value = "/authorisations",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateCommand authenticateCommand) {

		PendingAuthorisation pendingAuthorisation = null;

		//Mobile ID
		if (authenticateCommand.getPhoneNumber() != null) {
			MobileIDSession mobileIDSession = mobileIdAuthService.startLogin(authenticateCommand.getPhoneNumber());
			pendingAuthorisation = pendingAuthorisationService.store(
					// TODO: Use better types here (not strings)
					//authenticateCommand.getAccountPublicKey(),
					Hex.toHexString(authenticateCommand.getAccountAddress()),
					mobileIDSession);
		} //Bank transfer
		else {
			pendingAuthorisation = pendingAuthorisationService.store(
					//authenticateCommand.getAccountPublicKey(),
					Hex.toHexString(authenticateCommand.getAccountAddress()));
		}

		return new ResponseEntity<AuthenticateResponse>(AuthenticateResponse.fromPendingAuthorisation(pendingAuthorisation), HttpStatus.OK);
	}

	@ApiOperation(value = "Associate created address with ID card")
	@RequestMapping(
			method = POST,
			value = "/authorisations/idCards",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AccountActivationResponse> authenticateIdCard(@Valid @RequestBody AuthenticateCommand authenticateCommand, Principal principal) throws JSONException, IOException {
		//if (!principal) throw SOME CUSTOM EXCEPTION OF CARD NOT PRESENT
		String ownerId = principal.getName();
		HttpStatus status = HttpStatus.OK;
		String txHash = new String();
		EthereumAccount account = new EthereumAccount();
		List<EthereumAccount> existingAccounts = accountManagementService.getAccountsByAccountAddress(Hex.toHexString(authenticateCommand.getAccountAddress()));
		if(existingAccounts.size() == 0) {
			account = accountManagementService.storeNewAccount(Hex.toHexString(authenticateCommand.getAccountAddress()), ownerId, AuthorisationType.ID_CARD);
			if(accountActivationEnabled) {
				try {
					txHash = ethereumService.activateEthereumAccount(account.getAddress());
					accountManagementService.markActivated(account,txHash);
				} catch (IOException e) {
					log.error("failed to activate account "+ account.getAddress()+" on Ethereum", e);
					status = HttpStatus.INTERNAL_SERVER_ERROR;
				}
			}
		} else {
			log.error("Refusing to activate account: {0} binding(s) found already", existingAccounts.size());
			status = HttpStatus.BAD_REQUEST;
		}

		return new ResponseEntity<>(AccountActivationResponse.builder()
						.authenticationStatus(AuthenticationStatus.LOGIN_SUCCESS.name())
						.ownerId(ownerId)
						.transactionHash(txHash)
	  					.escrowTransfers(clearEscrow(account))
						.build(), status);
	}

	@ApiOperation(value = "Submit signed authIdentifier and get bank transfer payment reference for account activation")
	@RequestMapping(
			method = POST,
			value = "/authorisations/paymentReferences",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<PaymentReferenceResponse> getBankTransferReference(@Valid @RequestBody GetPaymentReferenceCommand cmd) {

		PendingAuthorisation pendingAuthorisation = pendingAuthorisationService.findByAuthIdentifier(cmd.getAuthIdentifier());
		if (pendingAuthorisation == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (pendingAuthorisation.getBankTransferPaymentReference() == null) {
			pendingAuthorisationService.addPaymentReferenceToPendingAuthorisation(pendingAuthorisation);
		}

		return new ResponseEntity<PaymentReferenceResponse>(PaymentReferenceResponse.fromPendingAuthorisation(pendingAuthorisation), HttpStatus.OK);
	}

	@ApiOperation(value = "[Mobile ID polling endpoint] Validate authorisation, store new account-identity mapping and activate ethereum account")
	@RequestMapping(
			method = POST,
			value = "/accounts",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AccountActivationResponse> authorizeMobileIdAndCreateAccountIdentityMapping(@Valid @RequestBody PollCommand pollCommand) throws IOException, JSONException {
		PendingAuthorisation pendingAuthorisation = pendingAuthorisationService.findByAuthIdentifier(pollCommand.getAuthIdentifier());

		AccountActivationResponse.AccountActivationResponseBuilder responseBuilder = AccountActivationResponse.getBuilderForAuthType(AuthorisationType.MOBILE_ID);

		if (pendingAuthorisation == null) {
			return new ResponseEntity<AccountActivationResponse>(responseBuilder.authenticationStatus(AuthenticationStatus.LOGIN_EXPIRED.name()).build(), HttpStatus.OK);
		}

		MobileIDSession mobileIDSession = MobileIDSession.fromString(pendingAuthorisation.getSerialisedMobileIdSession());
		String accountAddress = pendingAuthorisation.getAddress();

		if (mobileIDSession == null || accountAddress == null) {
			return new ResponseEntity<AccountActivationResponse>(responseBuilder.authenticationStatus(AuthenticationStatus.LOGIN_EXPIRED.name()).build(), HttpStatus.OK);
		}

		responseBuilder.ownerId(mobileIDSession.personalCode);

		// TODO: Make a better API here, so that the client would not have to submit the signature every time.
		if(StringUtils.hasText(pollCommand.getSignature())) { // TODO: Remove this if-statement when client-side is finished (i.e., make signatures mandatory)
			if(!pendingAuthorisation.verifyChallengeSignedByEthereumAccountHolder(pollCommand.getSignatureParsedForm())) {
				return new ResponseEntity<AccountActivationResponse>(responseBuilder.authenticationStatus(AuthenticationStatus.LOGIN_INVALID_SIGNATURE.name()).build(), HttpStatus.OK);
			}
		}

		// Check if authenticated
		if (mobileIdAuthService.isLoginComplete(mobileIDSession)) {
			pendingAuthorisationService.expire(pendingAuthorisation);
		} else {
			return new ResponseEntity<AccountActivationResponse>(responseBuilder.authenticationStatus(AuthenticationStatus.LOGIN_PENDING.name()).build(), HttpStatus.OK);
		}

		EthereumAccount newAccount;
		String txHash = new String();
		try {
			newAccount = accountManagementService.storeNewAccount(accountAddress, mobileIDSession.personalCode, AuthorisationType.MOBILE_ID);

			if (accountActivationEnabled) {
				txHash = ethereumService.activateEthereumAccount(accountAddress);
			}
		} catch (Exception e) {
                        log.error("Login failure", e);
			return new ResponseEntity<AccountActivationResponse>(responseBuilder.authenticationStatus(AuthenticationStatus.LOGIN_EXPIRED.name()).build(), HttpStatus.OK);
		}

		accountManagementService.markActivated(newAccount,txHash);

		return new ResponseEntity<AccountActivationResponse>(responseBuilder
				.authenticationStatus(AuthenticationStatus.LOGIN_SUCCESS.name())
				.transactionHash(txHash)
	  			.escrowTransfers(clearEscrow(newAccount))
				.build(), 
			HttpStatus.OK);
	}

	@ApiOperation(value = "[Bank Transfer based account registration] Validate signed authIdentifier, store new account-identity mapping and activate ethereum account [BASIC AUTH]")
	@RequestMapping(
			method = PUT,
			value = "/accounts",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AccountActivationResponse> authorizeBankTransferAndCreateAccountIdentityMapping(@Valid @RequestBody BankTransferBasedAccountRegistrationCommand cmd,
																										  @RequestHeader(value = "Authorization") String authorization) {
		AccountActivationResponse.AccountActivationResponseBuilder responseBuilder = AccountActivationResponse.getBuilderForAuthType(AuthorisationType.BANK_TRANSFER).ownerId(cmd.getOwnerId());

		if (!isAuthorized(authorization)) {
			log.error("Account registration via bank unauthorized");
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		PendingAuthorisation pendingAuthorisation = pendingAuthorisationService.findByPaymentReference(cmd.getPaymentReference());
		if (pendingAuthorisation == null) {
			return new ResponseEntity<AccountActivationResponse>(responseBuilder.authenticationStatus(AuthenticationStatus.LOGIN_EXPIRED.name()).build(), HttpStatus.OK);
		}

		EthereumAccount newAccount;
		String txHash = new String();
		try {
			newAccount = accountManagementService.storeNewAccount(pendingAuthorisation.getAddress(), cmd.getOwnerId(), AuthorisationType.BANK_TRANSFER);

			if (accountActivationEnabled) {
				txHash = ethereumService.activateEthereumAccount(newAccount.getAddress());
			}
		} catch (Exception e) {
			log.error("Login failure", e);
			return new ResponseEntity<AccountActivationResponse>(responseBuilder.authenticationStatus(AuthenticationStatus.LOGIN_EXPIRED.name()).build(), HttpStatus.OK);
		}


		accountManagementService.markActivated(newAccount,txHash);
		pendingAuthorisationService.expire(pendingAuthorisation);

		return new ResponseEntity<AccountActivationResponse>(responseBuilder.authenticationStatus(AuthenticationStatus.LOGIN_SUCCESS.name()).build(), HttpStatus.OK);
	}


	@ApiOperation(value = "View existing accounts")
	@RequestMapping(method = GET, value = "/accounts")
	public ResponseEntity<AccountsResponse> listAccounts(
			@RequestParam(name = "ownerId", required = false) String ownerId,
			@RequestParam(name = "escrow", required = false) boolean escrow,
			@RequestParam(name = "inactive", required = false) boolean inactive
	) {
		if (ownerId != null) {
			return new ResponseEntity<>(AccountsResponse.fromEthereumAccounts(accountManagementService.getAccountsByOwnerIdActiveEscrow(ownerId,inactive,escrow)), HttpStatus.OK);
		}
		return new ResponseEntity<>(AccountsResponse.fromEthereumAccounts(emptyList()), HttpStatus.BAD_REQUEST);
	}

	@ApiOperation(value = "Remove account identity mapping")
	@RequestMapping(method = DELETE, value = "/accounts")
	public ResponseEntity<AccountsResponse> removeAccount(
			@RequestParam(name = "mappingId", required = true) Long mappingId,
			@RequestHeader(value = "Authorization") String authorization) {

		if (!isAuthorized(authorization)) {
			log.error("Account deletion unauthorized");
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		accountManagementService.removeAccountById(mappingId);
		return new ResponseEntity<>(AccountsResponse.fromEthereumAccounts(accountManagementService.getAllAccounts()), HttpStatus.OK);
	}

    private boolean isAuthorized(String authorization) {
        return authorization != null ;
    }

	private List<EscrowTransfer> clearEscrow(EthereumAccount account) throws IOException,JSONException {

	  	long idCode = Long.parseLong(account.getOwnerId());
		String address  = account.getAddress();
		// check if escrow
		List<EscrowTransfer> etxs;
		if ( account.getAuthorisationType() != AuthorisationType.ESCROW && escrowService.getExistingEscrow(idCode) != null ) {
			return escrowService.clearAllToAddress(idCode,address);
		} else return null;
	}
}
