package com.kryptoeuro.accountmapper.response;

import com.codeborne.security.mobileid.MobileIDSession;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kryptoeuro.accountmapper.domain.PendingAuthorisation;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticateResponse {
	UUID authIdentifier;
	String mobileIdChallengeCode;
	String authorisationType;

	public static AuthenticateResponse fromPendingAuthorisation(PendingAuthorisation pendingAuthorisation) {
		AuthenticateResponseBuilder responseBuilder = AuthenticateResponse.builder()
				.authIdentifier(pendingAuthorisation.getAuthIdentifier())
				.authorisationType(pendingAuthorisation.getType().name());

		if (pendingAuthorisation.getSerialisedMobileIdSession() != null) {
			responseBuilder.mobileIdChallengeCode(MobileIDSession.fromString(pendingAuthorisation.getSerialisedMobileIdSession()).challenge);
		}

		return responseBuilder.build();
	}
}
