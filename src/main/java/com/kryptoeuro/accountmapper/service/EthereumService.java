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
import org.ethereum.crypto.ECKey;
import org.ethereum.util.ByteUtil;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
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

//	private String contractAddress = "0xAF8ce136A244dB6f13a97e157AC39169F4E9E445"; // contract 0.31
	private String contractAddress = "0x2FdAB8f12fA9Ad9Ad91fc55d52569AFc98Be9831"; // contract 0.41
	private String jsonRpcUrl = "http://54.194.239.231:8545"; // Parity node on AWS

	private Function approveAccountFunction = Function.fromSignature("approveAccount", "address");
	private Function appointAccountApproverFunction = Function.fromSignature("appointAccountApprover", "address");

	public void activateEthereumAccount(String accountAddress) throws IOException {
		accountAddress = with0x(accountAddress);
		ECKey approver = getAccountApproverKey();
		String txHash = sendTransaction(approver, approveAccountFunction.encode(accountAddress));
		log.info("Account " + accountAddress + " approved by " + hex(approver.getAddress()) + ". TxHash=" + txHash);
	}

	public void transferAccountApprovalRight(String newAddress) throws IOException {
		newAddress = with0x(newAddress);
		ECKey approver = getAccountApproverKey();
		String txHash = sendTransaction(approver, appointAccountApproverFunction.encode(newAddress));
		log.info("Account approval right transferred to " + newAddress + ". You must change your account approver key file accordingly. TxHash=" + txHash);
	}

	private String sendTransaction(ECKey signer, byte[] callData) throws IOException {
		long transactionCount = getTransactionCount(hex(signer.getAddress()));
		byte[] nonce = ByteUtil.longToBytesNoLeadZeroes(transactionCount);

		byte[] gasPrice = ByteUtil.longToBytesNoLeadZeroes(30000000000L);
		byte[] gasLimit = ByteUtil.longToBytesNoLeadZeroes(200000);

		byte[] toAddress = Hex.decode(without0x(contractAddress));

		Transaction transaction = new Transaction(nonce, gasPrice, gasLimit, toAddress, null, callData);
		//noinspection ConstantConditions
		transaction.sign(signer.getPrivKeyBytes());
		return send(json("eth_sendRawTransaction", hex(transaction.getEncoded())));
	}

	private String hex(byte[] bytes) {
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

	private String without0x(String hex) {
		return hex.startsWith("0x") ? hex.substring(2) : hex;
	}

	private String with0x(String hex) {
		return hex.startsWith("0x") ? hex : "0x" + hex;
	}

	private String toString(InputStream stream) throws IOException {
		try (InputStream is = stream) {
			return new Scanner(is).useDelimiter("\\A").next();
		}
	}
}
