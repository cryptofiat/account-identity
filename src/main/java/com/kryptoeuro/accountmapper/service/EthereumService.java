package com.kryptoeuro.accountmapper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class EthereumService {
	public void activateEthereumAccount(String accountAddress) {
		//Magic communication with ethereum universe
	}
}
