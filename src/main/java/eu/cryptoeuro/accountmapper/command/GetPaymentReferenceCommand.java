package eu.cryptoeuro.accountmapper.command;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
public class GetPaymentReferenceCommand {
	@NotNull
	UUID authIdentifier;
	@NotNull
	String signedAuthIdentifier;
}
