package com.kryptoeuro.accountmapper.command;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
public class BankTransferBasedAccountRegistrationCommand {
	@NotNull
	UUID authIdentifier;
	@NotNull
	String ownerId;
}
