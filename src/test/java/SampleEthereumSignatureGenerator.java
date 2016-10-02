import static com.kryptoeuro.accountmapper.TestUtils.convertEthereumSignatureToDer;
import static org.ethereum.crypto.ECKey.CURVE;
import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.bytesToBigInteger;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

public class SampleEthereumSignatureGenerator {
    public static void main(String[] args) {
        String signedData = "6b2168da-3dff-4e06-b05c-e39f91bd1eae";

        ECKey usersPrivateKey = new ECKey(
                bytesToBigInteger(Hex.decode("f092460bdfe1229f3c7c70e80a3722dd526b969056700f908e91d9f8178e1464")),
                CURVE.getCurve().createPoint(
                        bytesToBigInteger(Hex.decode("0F 7E C4 84 DA 6B 82 B5 3B 75 C1 57 30 BD C4 A2 6D 7E 18 43 65 E0 01 50 46 65 A2 C2 E1 82 B8 72")),
                        bytesToBigInteger(Hex.decode("00 8A 4B F8 48 EA F7 9C 4D 7B 3F 30 7E A6 8D B6 55 75 F5 FD D7 F9 8F 65 CD 76 1A E0 27 61 D9 EC F0")))
        );

        ECKey.ECDSASignature signature = usersPrivateKey.sign(sha3(signedData.getBytes()));
        byte[] signautureAsDer = convertEthereumSignatureToDer(Base64.decode(signature.toBase64()));

        ECKey publicKey = ECKey.fromPublicOnly(usersPrivateKey.getPubKeyPoint());

        System.out.println("Private key: " + Hex.toHexString(usersPrivateKey.getPrivKeyBytes()));
        System.out.println("Public key (65 bytes): " + Hex.toHexString(publicKey.getPubKey()));
        System.out.println("Address: " + Hex.toHexString(publicKey.getAddress()));

        System.out.println("Signature in DER: " + Hex.toHexString(signautureAsDer));
    }
}
