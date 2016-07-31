package com.kryptoeuro.accountmapper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.ethereum.core.CallTransaction.Function;
import org.ethereum.core.Transaction;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class EthereumService {

	private HttpClient httpClient = HttpClientBuilder.create().build();

	private String contractAddress = "0xAF8ce136A244dB6f13a97e157AC39169F4E9E445";
	private String jsonRpcUrl = "http://54.194.239.231:8545";

	private Function approveFunction = Function.fromSignature("approveAccount", "address", "bool");

	public void activateEthereumAccount(String accountAddress) throws IOException {
		accountAddress = with0x(accountAddress);

		long transactionCount = getTransactionCount(with0x(getAccountApproverAddress()));
		byte[] nonce = ByteUtil.longToBytesNoLeadZeroes(transactionCount);

		byte[] gasPrice = ByteUtil.longToBytesNoLeadZeroes(30000000000L);
		byte[] gasLimit = ByteUtil.longToBytesNoLeadZeroes(200000);

		byte[] toAddress = Hex.decode(without0x(contractAddress));
		byte[] callData = approveFunction.encode(accountAddress, true);

		Transaction transaction = new Transaction(nonce, gasPrice, gasLimit, toAddress, null, callData);
		transaction.sign(getAccountApproverPrivateKey());

		String txHash = send(json("eth_sendRawTransaction", with0x(Hex.toHexString(transaction.getEncoded()))));
		log.info("Account " + accountAddress + " approved. TxHash=" + txHash);
	}

	private byte[] getAccountApproverPrivateKey() {
		return Hex.decode("71e2dc69c297ffdd8fe2ef86b4225ebe27b3d259e25e94a2c45f2e29a97d84f0");
	}

	private String getAccountApproverAddress() {
		return "0x03e5403a7a733c3aa820d10a075df47ae73fa83a";
	}

	private long getTransactionCount(String account) throws IOException {
		String result = send(json("eth_getTransactionCount", account, "latest"));
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
		return resultFromJson(new Scanner(stream).useDelimiter("\\A").next());
	}

	private String resultFromJson(String json) throws IOException {
		JsonNode jsonNode = new ObjectMapper().readTree(json);
		if (!jsonNode.get("jsonrpc").asText().equals("2.0"))
			throw new IOException("Unknown json response: " + json);
		return jsonNode.get("result").asText();
	}

	private String without0x(String hex) {
		return hex.startsWith("0x") ? hex.substring(2) : hex;
	}

	private String with0x(String hex) {
		return hex.startsWith("0x") ? hex : "0x" + hex;
	}
}
