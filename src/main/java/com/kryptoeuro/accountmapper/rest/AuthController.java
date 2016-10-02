package com.kryptoeuro.accountmapper.rest;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

	@RequestMapping(value = "/idCard", produces = MediaType.APPLICATION_JSON_VALUE)
	public String idCard(Principal principal) {
		if(principal == null) {
			throw new IllegalArgumentException("No principal in request. Bad configuration?");
		}
		return principal.getName();
	}
}
