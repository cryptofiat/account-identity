package com.kryptoeuro.accountmapper.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kryptoeuro.accountmapper.domain.AuthorisationType;
import com.kryptoeuro.accountmapper.state.AuthenticationStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountActivationResponse {
	String authenticationStatus;
	String authorisationType;
	String ownerId;

	@Builder
	public AccountActivationResponse(AuthenticationStatus status, AuthorisationType type, String ownerId) {
		this.authenticationStatus = status.name();
		this.authorisationType = type.name();
		this.ownerId = ownerId;
	}

	public static AccountActivationResponseBuilder getBuilderForAuthType(AuthorisationType authType) {
		return AccountActivationResponse.builder().authorisationType(authType.name());
	}
}
