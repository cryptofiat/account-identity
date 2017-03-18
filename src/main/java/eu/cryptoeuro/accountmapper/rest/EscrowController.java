package eu.cryptoeuro.accountmapper.rest;

import eu.cryptoeuro.accountmapper.response.AccountActivationResponse;
import eu.cryptoeuro.accountmapper.service.LdapService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;
import java.io.IOException;

import eu.cryptoeuro.accountmapper.error.LdapNotFoundException;
import eu.cryptoeuro.accountmapper.error.HasActiveAccountException;

import eu.cryptoeuro.accountmapper.domain.EthereumAccount;
import eu.cryptoeuro.accountmapper.state.AuthenticationStatus;
import eu.cryptoeuro.accountmapper.service.EscrowService;
import eu.cryptoeuro.accountmapper.service.AccountManagementService;

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
	public ResponseEntity<AccountActivationResponse> getEscrow(@PathVariable("idCode") @Valid long idCode) throws IOException {
		if (accountService.hasActivatedAccount(idCode)) { throw new HasActiveAccountException(); };
		if (ldapService.lookupIdCode(idCode) == null) { throw new LdapNotFoundException(); };

		EthereumAccount account;

		account = escrowService.approveEscrowAccountForId(idCode);

		AccountActivationResponse aaResponse = AccountActivationResponse.builder()
						.authenticationStatus(AuthenticationStatus.LOGIN_SUCCESS.name())
						.ownerId(account.getOwnerId())
						.address(account.getAddress())
						.transactionHash(account.getTransactionHash())
						.build();
		return new ResponseEntity<AccountActivationResponse>(aaResponse, HttpStatus.OK);
	}
}
