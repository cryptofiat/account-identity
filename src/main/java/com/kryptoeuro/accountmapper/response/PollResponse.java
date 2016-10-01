package com.kryptoeuro.accountmapper.response;

import com.kryptoeuro.accountmapper.state.AuthenticationStatus;
import lombok.Builder;
import lombok.Data;

@Data
public class PollResponse {
	String authenticationStatus;

	@Builder
	public PollResponse(AuthenticationStatus authenticationStatus) {
		this.authenticationStatus = authenticationStatus.name();
	}
}
