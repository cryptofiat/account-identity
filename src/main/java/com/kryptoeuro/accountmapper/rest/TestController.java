package com.kryptoeuro.accountmapper.rest;

import com.codeborne.security.mobileid.MobileIDSession;
import com.kryptoeuro.accountmapper.service.MobileIdAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    MobileIdAuthService mobileIdAuthService;

//  Use 60000007 or 53477254 for testing
//  https://demo.sk.ee/MIDCertsReg/index.php <- register your phone number here
    @RequestMapping(value = "/mobileid")
    public String testMobileIdLogin(@RequestParam(name = "mobileNumber") String mobileNumber) {
        MobileIDSession mobileIDlogin = mobileIdAuthService.fullLogin(mobileNumber);

        return mobileIDlogin != null
                ? "FirstName: " + mobileIDlogin.firstName +
                    "\nLastName: " + mobileIDlogin.lastName +
                    "\nID: " + mobileIDlogin.personalCode
                : "Could not login";
    }

    @RequestMapping(value = "/addToSession")
    public String addToSession(@RequestParam(name = "objectValue") String value, HttpSession httpSession) {
        httpSession.removeAttribute("sessionObject");
        httpSession.setAttribute("sessionObject", value);
        return "OK";
    }

    @RequestMapping(value = "/getFromSession")
    public String getFromSession(HttpSession httpSession) {
        Object value = httpSession.getAttribute("sessionObject");
        return value != null ? value.toString() : "Value has not found in session. Use /test/addToSession?sessionObject=yourvalue to add it.";
    }
}
