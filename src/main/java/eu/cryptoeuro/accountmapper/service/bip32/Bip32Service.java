package eu.cryptoeuro.accountmapper.service.bip32;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.pqc.math.linearalgebra.ByteUtils;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.bitcoinj.core.Utils.HEX;

@Service
@Slf4j
public class Bip32Service {

    String PRIVATE_KEY_PREFIX = "xprv";

    private String getChild(String extendedPublicKey, int i) {

        String extendedPublicKeyHex = HEX.encode(Base58.decode(extendedPublicKey));
        log.info("Extended public key HEX {}", extendedPublicKeyHex);

        byte[] key = decodePrivateKeyFromWIF(Base58.encode(getWifKey(extendedPublicKeyHex)));

        DeterministicKey masterPublicKey = HDKeyDerivation.createMasterPubKeyFromBytes(
                //getPublicKey(extendedPublicKeyHex),
                key,
                getChainCode(extendedPublicKeyHex)
        );

        return null;
    }

    private byte[] getChainCode(String extendedPublicKeyHex) {
//        String slice = extendedPublicKeyHex.substring(4+8+2+8+8, 64);
        String slice = extendedPublicKeyHex.substring(26, 26+64);
        log.info("Chain code: " + slice);
        return HEX.decode(slice);
    }

    private byte[] getWifKey(String extendedPublicKeyHex) {
        return HEX.decode(extendedPublicKeyHex.substring(26+64));
    }

    private byte[] getPublicKey(String extendedPublicKeyHex) {
//        String slice =  extendedPublicKeyHex.substring(4+8+2+8+8+64);

        String pubPrivIndicator = extendedPublicKeyHex.substring(26+64, 26+64+2);

//        String slice =  extendedPublicKeyHex.substring(26+66, 26+66+66);

        String slice =  extendedPublicKeyHex.substring(26+64+2, 26+64+2+64);//drop checksum bytes from the end as well
//        slice = slice.substring(0, 62);
        log.info("Public key: " + slice);
//        log.info(HEX.encode(Base58.decode("KzJp5B7mDpZ7kMHv67GowQRys9W9Hbaa5Rzj4PCoiyXfTk1fGAvH")));
//        log.info("Public key: " + Base58.encode(HEX.decode(slice)));
        return HEX.decode(slice);
    }

    private String generateMasterKey(String seed) {
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(HEX.decode(seed));
        return PRIVATE_KEY_PREFIX + getExtendedKey(
                masterPrivateKey.getChainCode(),
                masterPrivateKey.getPrivKeyBytes()
        );
    }

    private String getExtendedKey(byte[] chainCode, byte[] privateKey) {
        String version = "0488ade4";
        String depth = "00";
        String fingerprint = "00000000";
        String childNumber = "00000000";

        byte[] extendedKey = HEX.decode((new StringBuilder())
                .append(version)
                .append(depth)
                .append(fingerprint)
                .append(childNumber)
                .append(HEX.encode(chainCode))
                .append(HEX.encode(privateKey))
                .toString());

        return Base58.encode(extendedKey);
    }



    private DeterministicKeyChain generateDeterministicKeyChain() throws NoSuchAlgorithmException {

        SecureRandom secureRandom = SecureRandom.getInstanceStrong();

        DeterministicKeyChain deterministicKeyChain = new DeterministicKeyChain(secureRandom);

        return deterministicKeyChain;
        /*
        DeterministicSeed seed = new DeterministicSeed();
        KeyCrypter crypter;

        DeterministicKeyChain chain = new DeterministicKeyChain(seed, crypter);

        return chain;
        */
    }


    /**
     * Converts a private key "Wallet Import Format" (as used by Bitcoin) into an ECPrivateKey object. The process to do
     * so is as follows: <br><br>
     *
     * 1) Convert the Base58 encoded String into byte[] form.<br><br>
     * 2) Drop the last four bytes, which are the checksum.<br><br>
     * 3) Check that the checksum is valid for the remaining bytes.<br><br>
     * 4) Drop the first byte, which is the special value prepended to the key bytes during the WIF encoding process.<br><br>
     * 5) Check that the first byte equates to the decimal value 128.<br><br>
     * 6) The remaining bytes are the private key in two's complement form. Convert them into a BigInteger <br><br>
     * 7) Use newly created BigInteger value to create a new ECPrivateKey object.<br><br>
     *
     * <b>NOTE:</b> Somewhat confusingly, Bitmessage uses SHA-512 for its address generation and proof of work,
     * but uses SHA-256 for converting private keys into wallet import format.
     *
     * @param wifPrivateKey - A String representation of the private key in "Wallet Import Format"
     *
     * @return An ECPrivateKey object containing the private key
     */
    public byte[] decodePrivateKeyFromWIF (String wifPrivateKey)
    {
        byte[] privateKeyBytes = Base58.decode(wifPrivateKey);

        byte[] privateKeyWithoutChecksum = Arrays.copyOfRange(privateKeyBytes, 0, (privateKeyBytes.length - 4));

        byte[] checksum = Arrays.copyOfRange(privateKeyBytes, (privateKeyBytes.length - 4), privateKeyBytes.length);

        byte[] hashOfPrivateKey = SHA256.doubleDigest(privateKeyWithoutChecksum);

        byte[] testChecksum = Arrays.copyOfRange(hashOfPrivateKey, 0, 4);

        /*
        if (Arrays.equals(checksum, testChecksum) == false)
        {
            throw new RuntimeException("While decoding a private key from WIF in KeyConverter.decodePrivateKeyFromWIF(), the checksum was " +
                    "found to be invalid. Something is wrong!");
        }
        */
        // Check that the prepended 128 byte is in place
        if (privateKeyWithoutChecksum[0] != (byte) 128)
        {
            throw new RuntimeException("While decoding a private key from WIF in KeyConverter.decodePrivateKeyFromWIF(), its prepended value " +
                    "was found to be invalid. Something is wrong!");
        }

        // Drop the prepended 128 byte
        byte[] privateKeyFinalBytes = Arrays.copyOfRange(privateKeyWithoutChecksum, 1, privateKeyWithoutChecksum.length);

        // If the decoded private key has a negative value, this means that it originally
        // began with a zero byte which was stripped off during the encodeToWIF process. We
        // must now restore this leading zero byte.
        BigInteger privateKeyBigIntegerValue = new BigInteger(privateKeyFinalBytes);
        if (privateKeyBigIntegerValue.signum() < 1)
        {
            byte[] valueToPrepend = new byte[1];
            valueToPrepend[0] = (byte) 0;

            privateKeyFinalBytes = concatenateByteArrays(valueToPrepend, privateKeyFinalBytes);
        }

        return privateKeyFinalBytes;

//        ECPrivateKey ecPrivateKey = reconstructPrivateKey(privateKeyFinalBytes);

//        return ecPrivateKey;
    }

    byte[] concatenateByteArrays(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }


}
