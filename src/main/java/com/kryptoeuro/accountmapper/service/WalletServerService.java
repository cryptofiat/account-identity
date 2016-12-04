package com.kryptoeuro.accountmapper.service;

import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.kryptoeuro.accountmapper.response.WalletServerAccountResponse;

@Service
@Slf4j
public class WalletServerService {

	private String walletServer = "http://wallet.euro2.ee:8080"; // wallet-server node on AWS

	public WalletServerAccountResponse getAccount(String address) {

		ObjectMapper mapper = new ObjectMapper();
		WalletServerAccountResponse obj = null; 
		try { 
			obj = mapper.readValue(new URL(walletServer+"/v1/accounts/"+address), WalletServerAccountResponse.class);
		} catch (Exception e) {
			log.error("Failed loading account data from wallet-server", e);
		}
		return obj;
	}
}
