package com.kryptoeuro.accountmapper.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.util.Collections;

public class IdCardUser extends User {
	IdCardUser(String idCode) {
		super(idCode, "not_used", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
	}
}
