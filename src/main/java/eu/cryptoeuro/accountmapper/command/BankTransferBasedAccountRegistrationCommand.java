package eu.cryptoeuro.accountmapper.command;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class BankTransferBasedAccountRegistrationCommand {
	@NotNull
	String ownerId;
	@NotNull
	String paymentReference;
}
