package com.kryptoeuro.accountmapper.rest;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;

import com.kryptoeuro.accountmapper.error.LdapNotFoundException;
import com.kryptoeuro.accountmapper.error.HasActiveAccountException;

import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.state.AuthenticationStatus;
import com.kryptoeuro.accountmapper.response.AccountActivationResponse;
import com.kryptoeuro.accountmapper.service.EscrowService;
import com.kryptoeuro.accountmapper.service.LdapService;
import com.kryptoeuro.accountmapper.service.AccountManagementService;

@RestController
@RequestMapping("/v1/escrow")
@Slf4j
@CrossOrigin(origins = "*")
public class EscrowController {

	@Autowired
	EscrowService escrowService;
	@Autowired
	LdapService ldapService;
	@Autowired
	AccountManagementService accountService;


	@RequestMapping(method = RequestMethod.GET, value = "/{idCode}")
	public ResponseEntity<AccountActivationResponse> getEscrow(@PathVariable("idCode") @Valid long idCode) {
		if (accountService.hasActivatedAccount(idCode)) { throw new HasActiveAccountException(); };
		if (ldapService.lookupIdCode(idCode) == null) { throw new LdapNotFoundException(); };

		EthereumAccount account = escrowService.approveEscrowAccountForId(idCode);
		AccountActivationResponse aaResponse = AccountActivationResponse.builder()
						.authenticationStatus(AuthenticationStatus.LOGIN_SUCCESS.name())
						.ownerId(account.getOwnerId())
						.address(account.getAddress())
						.transactionHash(account.getTransactionHash())
						.build();
		return new ResponseEntity<AccountActivationResponse>(aaResponse, HttpStatus.OK);
	}
}
