package com.kryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;

@Data
public class AuthenticateResponse {
	String challengeCode;

	@Builder
	public AuthenticateResponse(String challengeCode) {
		this.challengeCode = challengeCode;
	}
}
