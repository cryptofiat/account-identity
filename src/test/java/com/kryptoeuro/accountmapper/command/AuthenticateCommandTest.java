package com.kryptoeuro.accountmapper.command;

import static org.junit.Assert.assertEquals;

import org.ethereum.crypto.ECKey;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

public class AuthenticateCommandTest {
    private AuthenticateCommand command = new AuthenticateCommand();
/*
    @Test
    public void publicKeyParsing() {
        command.setAccountPublicKey("040f7ec484da6b82b53b75c15730bdc4a26d7e184365e001504665a2c2e182b8728a4bf848eaf79c4d7b3f307ea68db65575f5fdd7f98f65cd761ae02761d9ecf0");

        ECKey parsedKey = command.getAccountPublicKeyParsedForm();
        assertEquals("f7ec484da6b82b53b75c15730bdc4a26d7e184365e001504665a2c2e182b872", parsedKey.getPubKeyPoint().getAffineXCoord().toString());
        assertEquals("8a4bf848eaf79c4d7b3f307ea68db65575f5fdd7f98f65cd761ae02761d9ecf0", parsedKey.getPubKeyPoint().getAffineYCoord().toString());

        assertEquals("87bbc91f436a2b4d10047ec333ef09cad67e00d5", Hex.toHexString(parsedKey.getAddress()));
    }
*/
}
