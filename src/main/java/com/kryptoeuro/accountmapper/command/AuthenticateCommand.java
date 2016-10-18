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
public class AuthenticateCommand {
	@NotNull
	String accountAddress; 
	//TODO: Address should be enough to verify the ecrecover signature
	//String accountPublicKey; // TODO: comment that this should be in ASN.1 format (e.g. 65 or 33 bytes), in hex; refactor the types here also.
	String phoneNumber;
    
    public ECKey getAccountPublicKeyParsedForm() {
        return ECKey.fromPublicOnly(Hex.decode("aabbccdd")); // just put in some random string
        //return ECKey.fromPublicOnly(Hex.decode(accountPublicKey));
    }

    public byte[] getAccountAddress() {
        log.info("Decoding in commmand: " + accountAddress);
        return Hex.decode(accountAddress);
        //return getAccountPublicKeyParsedForm().getAddress();
    }
}
