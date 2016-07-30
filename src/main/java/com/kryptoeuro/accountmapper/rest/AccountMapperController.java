package com.kryptoeuro.accountmapper.rest;

import com.codeborne.security.mobileid.MobileIDSession;
import com.kryptoeuro.accountmapper.command.AuthenticateCommand;
import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.service.AccountManagementService;
import com.kryptoeuro.accountmapper.service.EthereumService;
import com.kryptoeuro.accountmapper.service.MobileIdAuthService;
import com.kryptoeuro.accountmapper.response.AuthenticateResponse;
import com.kryptoeuro.accountmapper.response.PollResponse;
import com.kryptoeuro.accountmapper.state.PollResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.GET;


@RestController
@RequestMapping("/v1")
public class AccountMapperController {
	@Autowired
	MobileIdAuthService mobileIdAuthService;
	@Autowired
	EthereumService ethereumService;
	@Autowired
	AccountManagementService accountManagementService;

	private static final String HTTP_SESS_PAR_IDSESSION = "mobileIDSession";
	private static final String HTTP_SESS_PAR_ADDRESS = "address";

	@RequestMapping(value = "/", produces = "text/plain")
	public String index() {
		return "OK";
	}

	@RequestMapping(
			method = POST,
			value = "/authenticate",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateCommand authenticateCommand, HttpSession session) {
		// start mobile id auth
		MobileIDSession mobileIDSession = mobileIdAuthService.startLogin(authenticateCommand.getPhoneNumber());
		// save MobileIDSession and account address in HTTP session
		session.setAttribute(HTTP_SESS_PAR_IDSESSION, mobileIDSession);
		session.setAttribute(HTTP_SESS_PAR_ADDRESS, authenticateCommand.getAccountAddress());
		// return challenge;
		AuthenticateResponse authenticateResponse = new AuthenticateResponse(mobileIDSession.challenge);
		return new ResponseEntity<AuthenticateResponse>(authenticateResponse, HttpStatus.OK);
	}

	@RequestMapping(
			method = GET,
			value = "/poll")
	public ResponseEntity<PollResponse> poll(HttpSession httpSession) {
		// get MobileIDSession from HTTP session
		MobileIDSession mobileIDSession = (MobileIDSession) httpSession.getAttribute(HTTP_SESS_PAR_IDSESSION);
		String accountAddress = (String) httpSession.getAttribute(HTTP_SESS_PAR_ADDRESS);

		if (mobileIDSession == null || accountAddress == null) {
			return new ResponseEntity<PollResponse>(new PollResponse(PollResponseStatus.LOGIN_FAILURE), HttpStatus.OK);

		}

		// Check if authenticated
		boolean isAuthenticated = mobileIdAuthService.isLoginComplete(mobileIDSession);
		if (!isAuthenticated) {
			new ResponseEntity<PollResponse>(new PollResponse(PollResponseStatus.LOGIN_PENDING), HttpStatus.OK);
		}

		try {
			accountManagementService.storeNewAccount(accountAddress, mobileIDSession.personalCode);
			ethereumService.activateEthereumAccount(accountAddress);
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
