package com.kryptoeuro.accountmapper.command;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;

@Slf4j
@Data
@NoArgsConstructor
public class BackupChallengeCommand {
	@NotNull
	String plaintext; 
	String encrypted;
	String newEncrypted;
	Long idCode;
	Boolean active;
}
