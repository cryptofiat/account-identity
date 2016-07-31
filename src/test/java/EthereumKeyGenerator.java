import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

public class EthereumKeyGenerator {

  public static void main(String[] args) {
    ECKey ecKey = new ECKey();
    System.out.println("Private key:\n" + Hex.toHexString(ecKey.getPrivKeyBytes()));
    System.out.println("Address:\n0x" + Hex.toHexString(ecKey.getAddress()));
  }
}
