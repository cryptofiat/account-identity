package eu.cryptoeuro.accountmapper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.ethereum.core.CallTransaction.Function;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey;
import org.ethereum.crypto.HashUtil;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.InputStream;
import java.util.Scanner;
import java.io.ByteArrayOutputStream;

import eu.cryptoeuro.accountmapper.response.WalletServerAccountResponse;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class EthereumService {

	public static final int GAS_LIMIT = 200000;

	@Autowired
	WalletServerService wsService;

    @Value("${ethereum.node.url}")
    protected String jsonRpcUrl;

    @Value("${ethereum.contract.approve}")
    protected String contractAddress;

    @Value("${ethereum.contract.delegate}")
    protected String contractAddressForTransfers;


	private HttpClient httpClient = HttpClientBuilder.create().build();

	//Todo configure in yml
//	private String contractAddress = "0xAF8ce136A244dB6f13a97e157AC39169F4E9E445"; // contract 0.31
//	private String contractAddress = "0x2FdAB8f12fA9Ad9Ad91fc55d52569AFc98Be9831"; // contract 0.41
//	private String contractAddress = "0xC91F200C33de61FF7B9B930968d0B59C1b86DAf9"; // 2.10.2016 9:56PM
//	private String contractAddress = "0x76f86A0a55FB69970af5cB691c41F8bb8b722F52"; // 3.10.2016 11:45AM
//	private String contractAddress = "0xA5f9b79Fc7f067Df25A795685493514A295A8A81"; // 3.10.2016 15:14PM
//	private String contractAddressForTransfers = "0xaf71e622792f47119411ce019f4ca1b8d993496e"; // 3.10.2016 15:14PM

	private Function approveAccountFunction = Function.fromSignature("approveAccount", "address");
	private Function appointAccountApproverFunction = Function.fromSignature("appointAccountApprover", "address");
	private Function transferFunction = Function.fromSignature("transfer", "uint256", "address", "uint256", "uint256", "bytes", "address");

	public String activateEthereumAccount(String accountAddress) throws IOException {
		accountAddress = with0x(accountAddress);
		ECKey approver = getAccountApproverKey();
		String txHash = sendTransaction(approver, approveAccountFunction.encode(accountAddress));
		log.info("Account " + accountAddress + " approved by " + hex(approver.getAddress()) + ". TxHash=" + txHash);
		return txHash;
	}

	public void transferAccountApprovalRight(String newAddress) throws IOException {
		newAddress = with0x(newAddress);
		ECKey approver = getAccountApproverKey();
		String txHash = sendTransaction(approver, appointAccountApproverFunction.encode(newAddress));
		log.info("Account approval right transferred to " + newAddress + ". You must change your account approver key file accordingly. TxHash=" + txHash);
	}

	private String sendTransaction(ECKey signer, byte[] callData) throws IOException {
		return sendTransaction(signer, callData, contractAddress, 0);
	}

	private String sendTransaction(ECKey signer, byte[] callData, String _contract, int  nonceIncrement) throws IOException {
		//TODO: maybe a queue of pending transactions, otherwise one goes  through at a time
		long transactionCount = getTransactionCount(hex(signer.getAddress())) + nonceIncrement;
		long gasPriceWei = wsService.getGasPriceWei();
		log.info("Current gas price: " + String.valueOf(gasPriceWei));
		byte[] nonce = ByteUtil.longToBytesNoLeadZeroes(transactionCount);

		byte[] gasPrice = ByteUtil.longToBytesNoLeadZeroes(gasPriceWei);
		byte[] gasLimit = ByteUtil.longToBytesNoLeadZeroes(GAS_LIMIT);

		byte[] toAddress = Hex.decode(without0x(_contract));

		Transaction transaction = new Transaction(nonce, gasPrice, gasLimit, toAddress, null, callData);
		//noinspection ConstantConditions
		transaction.sign(signer.getPrivKeyBytes());
		return send(json("eth_sendRawTransaction", hex(transaction.getEncoded())));
	}

	protected String hex(byte[] bytes) {
		return with0x(Hex.toHexString(bytes));
	}

	private ECKey getAccountApproverKey() throws IOException {
		File file = new File(System.getProperty("user.home"), ".AccountApprover.key");
		try {
			String keyHex = toString(new FileInputStream(file));
			return ECKey.fromPrivate(Hex.decode(keyHex));
		}
		catch (IOException e) {
			throw new IOException("Cannot load account approver key. Make sure " + file.toString() + " exists and contains the private key in hex format.\n" + e.toString());
		}
	}

	private long getTransactionCount(String account) throws IOException {
		String result = send(json("eth_getTransactionCount", account, "latest"));
		return Long.parseLong(without0x(result), 16);
	}

    /* Deprecated: use wsService API instead */
	public Long getGasPriceWeiFromNetwork() throws IOException {
		String result = send(json("eth_gasPrice"));
		return Long.parseLong(without0x(result), 16);
	}

	private static String json(String method, String... params) {
		return "{" +
			"\"jsonrpc\":\"2.0\"," +
			"\"method\":\"" + method + "\"," +
			"\"params\": [" + stream(params).map(s -> "\"" + s + "\"").collect(joining(", ")) + "]," +
			"\"id\":1}";
	}

	private String send(String json) throws IOException {
		HttpPost request = new HttpPost(jsonRpcUrl);
		StringEntity params = new StringEntity(json);
		request.addHeader("content-type", "application/json");
		request.setEntity(params);

		HttpResponse response = httpClient.execute(request);
		if (response.getStatusLine().getStatusCode() != 200)
			throw new IOException(response.getStatusLine().toString());
		InputStream stream = response.getEntity().getContent();
		return resultFromJson(toString(stream));
	}

	private String resultFromJson(String json) throws IOException {
		JsonNode jsonNode = new ObjectMapper().readTree(json);
		if (!jsonNode.get("jsonrpc").asText().equals("2.0"))
			throw new IOException("Unknown json response: " + json);
		if (jsonNode.has("error"))
			throw new IOException(jsonNode.get("error").get("message").asText());
		if (!jsonNode.has("result"))
			throw new IOException("Cannot find 'result' in json: " + json);
		return jsonNode.get("result").asText();
	}

	protected String without0x(String hex) {
		return hex.startsWith("0x") ? hex.substring(2) : hex;
	}

	protected String with0x(String hex) {
		return hex.startsWith("0x") ? hex : "0x" + hex;
	}

	private String toString(InputStream stream) throws IOException {
		try (InputStream is = stream) {
			return new Scanner(is).useDelimiter("\\A").next();
		}
	}

	public byte[] uint256(long val) {
		ByteBuffer bytes = ByteBuffer.allocate(32);
		bytes.putLong(32-Long.BYTES,val);
		return bytes.array();
	}

	public void testSigning() throws IOException {
		
		String addr = "65fa6548764c08c0dd77495b33ed302d0c212691";

		ECKey signer = ECKey.fromPrivate(Hex.decode(without0x("0xc33d80b3fddd6bc5d62498905b90c94cf1252ffd846def3b530acd803bbb3783")));
		signDelegate(0,3,1,"65fa6548764c08c0dd77495b33ed302d0c212691",signer);
		WalletServerAccountResponse addrDetails = wsService.getAccount(hex(signer.getAddress())); 
		log.info("some info about address"+ addrDetails.toString());
	}
	
	//TODO: should write unit tests on hash and signature construction
	public byte[] signDelegate(long fee, long amount, long nonce, String address, ECKey signer) throws IOException {
		ByteArrayOutputStream hashInput = new ByteArrayOutputStream( );
		hashInput.write(uint256(nonce));
		hashInput.write(Hex.decode(without0x(address)));
		hashInput.write(uint256(amount));
		hashInput.write(uint256(fee));

		byte[] hashOutput = HashUtil.sha3(hashInput.toByteArray());
		String strSig = signer.sign(hashOutput).toBase64();

		// Because contract expects the sig concatenated in different order than canonical
		byte[] byteSig = new byte[65];
		System.arraycopy(Base64.decodeBase64(strSig),1,byteSig,0,64);
		System.arraycopy(Base64.decodeBase64(strSig),0,byteSig,64,1);
		return byteSig;
	}

	public String sendBalance(String toAddress, String privKey, int nonceIncrement) throws IOException {

		
		ECKey signer = ECKey.fromPrivate(Hex.decode(without0x(privKey)));
		WalletServerAccountResponse addrDetails = wsService.getAccount(hex(signer.getAddress())); 
		return sendBalance(toAddress,privKey,nonceIncrement,addrDetails.getBalance());
	}

	public String sendBalance(String toAddress, String privKey, int nonceIncrement, long amount) throws IOException {

		
		ECKey signer = ECKey.fromPrivate(Hex.decode(without0x(privKey)));
		ECKey sponsorKey = getAccountApproverKey(); 
		
		WalletServerAccountResponse addrDetails = wsService.getAccount(hex(signer.getAddress())); 

		byte[] signatureArg = signDelegate(0, amount, addrDetails.getNonce()+1 + nonceIncrement, without0x(toAddress), signer); 

		byte[] callData = transferFunction.encode(
			addrDetails.getNonce()+1 + nonceIncrement, 
			toAddress, 
			amount, 
			0, 
			signatureArg, 
			hex(sponsorKey.getAddress()));
	
		String txHash = sendTransaction(sponsorKey, callData, contractAddressForTransfers, nonceIncrement);
		log.info("Submitted transfer and got Tx= "+txHash);
		return txHash;
	}
}
