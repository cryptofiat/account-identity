package com.kryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
public class AuthenticateResponse {
	String challengeCode;
	UUID authIdentifier;

	@Builder
	public AuthenticateResponse(String challengeCode, UUID authIdentifier) {
		this.challengeCode = challengeCode;
		this.authIdentifier = authIdentifier;
	}
}
