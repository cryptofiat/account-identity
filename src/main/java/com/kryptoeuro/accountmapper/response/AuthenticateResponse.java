package com.kryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;

@Data
public class AuthenticateResponse {
	String challengeCode;
	String authIdentifier;

	@Builder
	public AuthenticateResponse(String challengeCode, String authIdentifier) {
		this.challengeCode = challengeCode;
		this.authIdentifier = authIdentifier;
	}
}
