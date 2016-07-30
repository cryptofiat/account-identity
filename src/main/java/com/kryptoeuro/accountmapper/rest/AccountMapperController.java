package com.kryptoeuro.accountmapper.rest;

import com.codeborne.security.mobileid.MobileIDSession;
import com.kryptoeuro.accountmapper.EthereumAccountRepository;
import com.kryptoeuro.accountmapper.command.AuthenticateCommand;
import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.mobileid.MobileIdAuthService;
import com.kryptoeuro.accountmapper.response.AuthenticateResponse;
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

@RestController
@RequestMapping("/v1")
public class AccountMapperController {
	@Autowired
	EthereumAccountRepository ethereumAccountRepository;
	@Autowired
	MobileIdAuthService mobileIdAuthService;

	@RequestMapping(value = "/", produces = "text/plain")
	public String index() {
		return "OK";
	}

	@RequestMapping(
			method = POST,
			value = "/authenticate/",
			consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<AuthenticateResponse> authenticate(@Valid @RequestBody AuthenticateCommand authenticateCommand, HttpSession session) {
		// start mobile id auth
		MobileIDSession mobileIDSession = mobileIdAuthService.startLogin(authenticateCommand.getPhoneNumber());
		// save MobileIDSession and account address in HTTP session
		session.setAttribute("mobileIDSession", mobileIDSession);
		session.setAttribute("address", authenticateCommand.getAccountAddress());
		// return challenge;
		AuthenticateResponse authenticateResponse = new AuthenticateResponse(mobileIDSession.challenge);
		return new ResponseEntity<AuthenticateResponse>(authenticateResponse, HttpStatus.OK);
	}

	@RequestMapping(value = "/poll")
	public String poll() {
		// get MobileIDSession from HTTP session
		// call MobileIDAuthentocator.isLoginComplete()
		// if true then accountAuthenticated(address from HTTP session, personalCode from mobile id session)
		return ""; // ok / nok
	}

	void accountAuthenticated(String address, String ownerId) {
		EthereumAccount account = new EthereumAccount(address, ownerId);
		ethereumAccountRepository.save(account);
		// activate account in ethereum contract
	}

	@RequestMapping(value = "/account/list")
	public List<EthereumAccount> listAccounts(@RequestParam(name = "ownerId") String ownerId) {
		return ethereumAccountRepository.findByOwnerId(ownerId);
	}
}
