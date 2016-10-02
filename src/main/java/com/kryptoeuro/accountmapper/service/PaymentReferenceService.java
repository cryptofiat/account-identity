package com.kryptoeuro.accountmapper.service;


import com.kryptoeuro.accountmapper.PendingAuthorisationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class PaymentReferenceService {

	@Autowired
	PendingAuthorisationRepository pendingAuthorisationRepository;

	String randomWordApiUrl = "http://www.setgetgo.com/randomword/get.php"; //Random word api

	public String getRandomPaymentReference() {
		return getRandomWord().toLowerCase() + " " + getRandomWord().toLowerCase();
	}

	private String getRandomWord() {
		try {
			URL url = new URL(randomWordApiUrl);
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			return is.toString();
		} catch (Exception e) {
			return UUID.randomUUID().toString();
		}
	}

}
