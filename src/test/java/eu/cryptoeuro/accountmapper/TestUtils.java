package eu.cryptoeuro.accountmapper;

import static org.ethereum.util.ByteUtil.bytesToBigInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.spongycastle.asn1.ASN1Encoding;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.DERSequence;

import java.io.IOException;
import java.math.BigInteger;

public class TestUtils {
    public static byte[] convertEthereumSignatureToDer(byte[] ethereumSignature) {
        BigInteger r = bytesToBigInteger(ArrayUtils.subarray(ethereumSignature, 1, 33));
        BigInteger s = bytesToBigInteger(ArrayUtils.subarray(ethereumSignature, 33, 65));

        try {
            return new DERSequence(new ASN1Integer[] {new ASN1Integer(r), new ASN1Integer(s)})
                    .getEncoded(ASN1Encoding.DER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
