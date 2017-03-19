package eu.cryptoeuro.accountmapper.service;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static org.bitcoinj.core.Utils.HEX;

@Service
@Slf4j
public class Bip32Service {

    String PRIVATE_KEY_PREFIX = "xprv";

    private String getChild(String extendedPublicKey, int i) {

        String extendedPublicKeyHex = HEX.encode(Base58.decode(extendedPublicKey));
        log.info("Extended public key HEX {}", extendedPublicKeyHex);

        DeterministicKey masterPublicKey = HDKeyDerivation.createMasterPubKeyFromBytes(
                getChainCode(extendedPublicKeyHex),
                getPublicKey(extendedPublicKeyHex)
        );

        return null;
    }

    private byte[] getChainCode(String extendedPublicKeyHex) {
//        String slice = extendedPublicKeyHex.substring(4+8+2+8+8, 64);
        String slice = extendedPublicKeyHex.substring(26, 26+64);
        log.info("Chain code: " + slice);
        return HEX.decode(slice);
    }

    private byte[] getPublicKey(String extendedPublicKeyHex) {
//        String slice =  extendedPublicKeyHex.substring(4+8+2+8+8+64);
        String slice =  extendedPublicKeyHex.substring(26+65, 26+64+66);
        log.info("Public key: " + slice);
        log.info(HEX.encode(Base58.decode("KzJp5B7mDpZ7kMHv67GowQRys9W9Hbaa5Rzj4PCoiyXfTk1fGAvH")));
        log.info("Public key: " + Base58.encode(HEX.decode(slice)));
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


}
