package com.kryptoeuro.accountmapper.rest;

import com.codeborne.security.mobileid.MobileIDSession;
import com.kryptoeuro.accountmapper.service.MobileIdAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.security.Principal;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*")
public class TestController {

    @RequestMapping("/idCardAuth")
    public String idCardAuth(HttpSession httpSession, Principal principal) {
        return principal != null ? principal.getName() : "nuffin";
    }
}
