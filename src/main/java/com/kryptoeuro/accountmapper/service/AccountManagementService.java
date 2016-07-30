package com.kryptoeuro.accountmapper.service;


import com.kryptoeuro.accountmapper.EthereumAccountRepository;
import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.error.CannotStoreAccountException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class AccountManagementService {

	@Autowired
	EthereumAccountRepository ethereumAccountRepository;

	public void storeNewAccount(String address, String ownerId) throws CannotStoreAccountException {
		EthereumAccount account = new EthereumAccount(address, ownerId);
		ethereumAccountRepository.save(account);
	}

	public List<EthereumAccount> getAccountsByOwnerId(String ownerId) {
		return ethereumAccountRepository.findByOwnerId(ownerId);
	}

	public List<EthereumAccount> getAccountsByAccountAddress(String accountAddress) {
		return ethereumAccountRepository.findByAddress(accountAddress);
	}
}
