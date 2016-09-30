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
import com.kryptoeuro.accountmapper.state.PollResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
public class AccountMapperController {
	@Autowired
	MobileIdAuthService mobileIdAuthService;
	@Autowired
	EthereumService ethereumService;
	@Autowired
	AccountManagementService accountManagementService;

	private static boolean accountActivationEnabled = false;

	@RequestMapping(value = "/", produces = "text/plain")
	public String index() {
		return "OK";
	}

	//Initial HttpSession approach did not work with marat's app. Will keep in memory here for now
	private static Map<String, PendingMobileIdAuthorisation> pendingAuthorisations = new HashMap<String, PendingMobileIdAuthorisation>();

	@RequestMapping(
			method = POST,
			value = "/authenticate",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateCommand authenticateCommand) {
		// start mobile id auth
		MobileIDSession mobileIDSession = mobileIdAuthService.startLogin(authenticateCommand.getPhoneNumber());

		pendingAuthorisations.put(mobileIDSession.challenge, new PendingMobileIdAuthorisation(mobileIDSession, authenticateCommand.getAccountAddress()));

		AuthenticateResponse authenticateResponse = new AuthenticateResponse(mobileIDSession.challenge, mobileIDSession.challenge); //todo change second parameter to unique identifier
		return new ResponseEntity<AuthenticateResponse>(authenticateResponse, HttpStatus.OK);
	}

	@RequestMapping(
			method = POST,
			value = "/poll",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<PollResponse> poll(@Valid @RequestBody PollCommand pollCommand) {
		PendingMobileIdAuthorisation pendingMobileIdAuthorisation = pendingAuthorisations.get(pollCommand.getAuthIdentifier());

		if (pendingMobileIdAuthorisation == null) {
			return new ResponseEntity<PollResponse>(new PollResponse(PollResponseStatus.LOGIN_EXPIRED), HttpStatus.OK);
		}

		MobileIDSession mobileIDSession = pendingMobileIdAuthorisation.mobileIdSession;
		String accountAddress = pendingMobileIdAuthorisation.address;

		if (mobileIDSession == null || accountAddress == null) {
			return new ResponseEntity<PollResponse>(new PollResponse(PollResponseStatus.LOGIN_EXPIRED), HttpStatus.OK);
		}

		// Check if authenticated
		boolean isAuthenticated = mobileIdAuthService.isLoginComplete(mobileIDSession);
		if (!isAuthenticated) {
			return new ResponseEntity<PollResponse>(new PollResponse(PollResponseStatus.LOGIN_PENDING), HttpStatus.OK);
		}

		try {
			accountManagementService.storeNewAccount(accountAddress, mobileIDSession.personalCode);
			pendingMobileIdAuthorisation.mobileIdSession = null;

			if (accountActivationEnabled) {
				ethereumService.activateEthereumAccount(accountAddress);
			}

			pendingMobileIdAuthorisation.address = null;
		} catch (Exception e) {
			return new ResponseEntity<PollResponse>(new PollResponse(PollResponseStatus.LOGIN_FAILURE), HttpStatus.OK);
		}

		return new ResponseEntity<PollResponse>(new PollResponse(PollResponseStatus.LOGIN_SUCCESS), HttpStatus.OK);
	}

	@RequestMapping(value = "/account/list")
	public List<EthereumAccount> listAccounts(@RequestParam(name = "ownerId") String ownerId) {
		return accountManagementService.getAccountsByOwnerId(ownerId);
	}
}
