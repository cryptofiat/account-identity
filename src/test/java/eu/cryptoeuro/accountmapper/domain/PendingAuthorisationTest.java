package eu.cryptoeuro.accountmapper.domain;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.ArrayUtils;
import org.ethereum.crypto.ECKey;

public class PendingAuthorisationTest {
    private ECKey usersPrivateKey = new ECKey();

/*
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
