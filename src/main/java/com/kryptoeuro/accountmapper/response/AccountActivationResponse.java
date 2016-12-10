package com.kryptoeuro.accountmapper.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kryptoeuro.accountmapper.domain.AuthorisationType;
import com.kryptoeuro.accountmapper.response.EscrowTransfer;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountActivationResponse {
	String authenticationStatus;
	String authorisationType;
	String transactionHash;
	String address;
	String ownerId;
	List<EscrowTransfer> escrowTransfers;

	public static AccountActivationResponseBuilder getBuilderForAuthType(AuthorisationType authType) {
		return AccountActivationResponse.builder().authorisationType(authType.name());
	}
}
