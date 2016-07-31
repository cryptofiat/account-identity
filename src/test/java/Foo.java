import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.ethereum.core.CallTransaction.Function;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public class Foo {

  private static HttpClient httpClient = HttpClientBuilder.create().build();

  public static void main(String[] args) throws IOException {
    byte[] privateKey = Hex.decode("71e2dc69c297ffdd8fe2ef86b4225ebe27b3d259e25e94a2c45f2e29a97d84f0");
    ECKey ecKey = ECKey.fromPrivate(privateKey);

    String myAddress = "03e5403a7a733c3aa820d10a075df47ae73fa83a";
    long transactionCount = getTransactionCount(myAddress);

    String contractAddress = "AF8ce136A244dB6f13a97e157AC39169F4E9E445";

    byte[] callData = Function.fromSignature("approveAccount", "address", "bool").encode("dcc4d964ca07022d4ce46ba97d3ce88544c04f66", true);

//    String kristoAddress = "cE8A7f7c35a2829C6554fD38b96A7fF43B0A76d6";
//    byte[] callData = Function.fromSignature("transfer", "address", "uint256").encode(kristoAddress, 2);

    byte[] nonce = ByteUtil.longToBytesNoLeadZeroes(transactionCount);
    byte[] gasPrice = ByteUtil.longToBytesNoLeadZeroes(30000000000L);
    byte[] gasLimit = ByteUtil.longToBytesNoLeadZeroes(200000);
    byte[] value = null;

    Transaction transaction = new Transaction(nonce, gasPrice, gasLimit, Hex.decode(contractAddress), value, callData);
    transaction.sign(ecKey.getPrivKeyBytes());

    String json = json("eth_sendRawTransaction", "0x" + Hex.toHexString(transaction.getEncoded()));

    System.out.println(send(json));
  }

  private static long getTransactionCount(String account) throws IOException {
    String result = send(json("eth_getTransactionCount", "0x" + account, "latest"));
    System.out.println("result = " + result);
    JsonNode jsonNode = new ObjectMapper().readTree(result);
    String valueHex = jsonNode.get("result").asText().replace("0x", "");
    return Long.parseLong(valueHex, 16);
  }

  private static String json(String method, String... params) {
    return "{" +
      "\"jsonrpc\":\"2.0\"," +
      "\"method\":\"" + method + "\"," +
      "\"params\": [" + stream(params).map(s -> "\"" + s + "\"").collect(joining(", ")) + "]," +
      "\"id\":1}";
  }

  private static String send(String json) throws IOException {
//    HttpPost request = new HttpPost("http://54.194.239.231:8545");
    HttpPost request = new HttpPost("http://localhost:8545");
    StringEntity params = new StringEntity(json);
    request.addHeader("content-type", "application/json");
    request.setEntity(params);
    HttpResponse response = httpClient.execute(request);
    if (response.getStatusLine().getStatusCode() != 200)
      throw new RuntimeException(response.getStatusLine().toString());
    InputStream stream = response.getEntity().getContent();
    return new Scanner(stream).useDelimiter("\\A").next();
  }
}
