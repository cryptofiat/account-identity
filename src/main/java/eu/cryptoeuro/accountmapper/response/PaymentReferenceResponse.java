package eu.cryptoeuro.accountmapper.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import eu.cryptoeuro.accountmapper.domain.PendingAuthorisation;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentReferenceResponse {
	UUID authIdentifier;
	String paymentReference;
	String authorisationType;

	public static PaymentReferenceResponse fromPendingAuthorisation(PendingAuthorisation pendingAuthorisation) {
		PaymentReferenceResponse.PaymentReferenceResponseBuilder responseBuilder = PaymentReferenceResponse.builder()
				.authIdentifier(pendingAuthorisation.getAuthIdentifier())
				.authorisationType(pendingAuthorisation.getType().name());

		if (pendingAuthorisation.getBankTransferPaymentReference() != null) {
			responseBuilder.paymentReference(pendingAuthorisation.getBankTransferPaymentReference());
		}

		return responseBuilder.build();
	}
}
