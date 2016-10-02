package com.kryptoeuro.accountmapper.domain;

import static com.kryptoeuro.accountmapper.TestUtils.convertEthereumSignatureToDer;
import static org.ethereum.crypto.HashUtil.sha3;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.kryptoeuro.accountmapper.TestUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.crypto.ECKey;
import org.junit.Test;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.util.UUID;

public class PendingAuthorisationTest {
    private ECKey usersPrivateKey = new ECKey();

    private PendingAuthorisation pendingAuthorisation = new PendingAuthorisation() {{
        setAuthIdentifier(UUID.randomUUID());

        setPublicKey(Hex.toHexString(usersPrivateKey.getPubKey()));
        setAddress(Hex.toHexString(usersPrivateKey.getAddress()));
    }};

    @Test
    public void successfulVerificationOfChallengeSignedByEthAccountHolder() {
        byte[] challenge = pendingAuthorisation.getChallengeForEthereumAccountHolder();
        ECKey.ECDSASignature signature = usersPrivateKey.sign(sha3(challenge));
        byte[] signatureAsDer = convertEthereumSignatureToDer(Base64.decode(signature.toBase64()));
        assertTrue(pendingAuthorisation.verifyChallengeSignedByEthereumAccountHolder(signatureAsDer));
    }
/*
    @Test
    public void failedVerificationOfChallengeSignedByEthAccountHolder_invalidSignature() {
        byte[] invalidSignature = convertEthereumSignatureToDer(new byte[65]);
        assertFalse(pendingAuthorisation.verifyChallengeSignedByEthereumAccountHolder(invalidSignature));
    }

    @Test
    public void failedVerificationOfChallengeSignedByEthAccountHolder_wrongChallengeSigned() {
        byte[] invalidChallenge = mutateChallenge(pendingAuthorisation.getChallengeForEthereumAccountHolder());
        ECKey.ECDSASignature signature = usersPrivateKey.sign(sha3(invalidChallenge));
        byte[] signatureAsDer = convertEthereumSignatureToDer(Base64.decode(signature.toBase64()));
        assertFalse(pendingAuthorisation.verifyChallengeSignedByEthereumAccountHolder(signatureAsDer));
    }
*/
    private byte[] mutateChallenge(byte[] challenge) {
        byte[] result = ArrayUtils.clone(challenge);
        result[0]++;
        return result;
    }

}
