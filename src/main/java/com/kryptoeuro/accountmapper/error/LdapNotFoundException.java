package com.kryptoeuro.accountmapper.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="This ID code was not found on the sk.ee ldap service.")
public class LdapNotFoundException extends RuntimeException {
}
