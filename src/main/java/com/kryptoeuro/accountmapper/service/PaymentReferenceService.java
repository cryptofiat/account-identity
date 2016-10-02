package com.kryptoeuro.accountmapper.service;


import com.kryptoeuro.accountmapper.PendingAuthorisationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class PaymentReferenceService {

	@Autowired
	PendingAuthorisationRepository pendingAuthorisationRepository;

	String randomWordApiUrl = "http://www.setgetgo.com/randomword/get.php"; //Random word api

	public String getRandomPaymentReference() {
		try {
			URL oracle = new URL(randomWordApiUrl);
			return new BufferedReader(new InputStreamReader(oracle.openStream())).readLine().toLowerCase()
					+ " "
					+ new BufferedReader(new InputStreamReader(oracle.openStream())).readLine().toLowerCase();
		} catch (Exception e) {
			return UUID.randomUUID().toString().toLowerCase();
		}
	}
}
