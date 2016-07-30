package com.kryptoeuro.accountmapper;

import com.codeborne.security.mobileid.MobileIDSession;
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

    @RequestMapping(value = "/account/create")
    public EthereumAccount createAccount(@RequestParam(name = "ownerId") String ownerId) {
        EthereumAccount account = new EthereumAccount(ownerId, "0xDEADBEEF");
        ethereumAccountRepository.save(account);
        return account;
    }

    @RequestMapping(value = "/account/list")
    public List<EthereumAccount> listAccounts(@RequestParam(name = "ownerId") String ownerId) {
        return ethereumAccountRepository.findByOwnerId(ownerId);
    }

//  Use 60000007 for testing
    @RequestMapping(value = "/test/mobileid")
    public String testMobileIdLogin(@RequestParam(name = "mobileNumber") String mobileNumber) {
        MobileIDSession mobileIDlogin = mobileIdAuthService.login(mobileNumber);

        return mobileIDlogin != null
                ? "FirstName: " + mobileIDlogin.firstName +
                    "\nLastName: " + mobileIDlogin.lastName +
                    "\nID: " + mobileIDlogin.personalCode
                : "Could not login";
    }
}
