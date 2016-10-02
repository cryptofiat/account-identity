package com.kryptoeuro.accountmapper.command;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class AuthenticateCommand {
	@NotNull
	String accountPublicKey; // TODO: comment that this should be in ASN.1 format (e.g. 65 or 33 bytes), in hex; refactor the types here also.
	String phoneNumber;

    public ECKey getAccountPublicKeyParsedForm() {
        return ECKey.fromPublicOnly(Hex.decode(accountPublicKey));
    }

    public byte[] getAccountAddress() {
        return getAccountPublicKeyParsedForm().getAddress();
    }
}
