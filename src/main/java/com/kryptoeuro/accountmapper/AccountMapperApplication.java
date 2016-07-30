package com.kryptoeuro.accountmapper;

import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.mobileid.MobileIdAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class AccountMapperApplication {

    @Autowired
    EthereumAccountRepository ethereumAccountRepository;
    @Autowired
    MobileIdAuthService mobileIdAuthService;

	public static void main(String[] args) {
		SpringApplication.run(AccountMapperApplication.class, args);
	}

    @RequestMapping(value = "/", produces = "text/plain")
    public String index() {
        return "OK";
    }

    @RequestMapping(value = "/authenticate")
    public String authenticate(@RequestParam(name = "address") String address, @RequestParam(name = "phoneNumber") String phoneNumber, HttpSession session) {
        // start mobile auth
        // save MobileIDSession in HTTP session
        // save address in HTTP session
        session.setAttribute("address", address);
        // return MobileIDSession.challenge;
        return "";
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
