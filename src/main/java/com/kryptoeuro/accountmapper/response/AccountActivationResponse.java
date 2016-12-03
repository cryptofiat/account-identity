package com.kryptoeuro.accountmapper.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kryptoeuro.accountmapper.domain.AuthorisationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountActivationResponse {
	String authenticationStatus;
	String authorisationType;
	String transactionHash;
	String ownerId;

	public static AccountActivationResponseBuilder getBuilderForAuthType(AuthorisationType authType) {
		return AccountActivationResponse.builder().authorisationType(authType.name());
	}
}
