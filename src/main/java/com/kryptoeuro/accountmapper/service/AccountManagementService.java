package com.kryptoeuro.accountmapper.service;


import com.kryptoeuro.accountmapper.EthereumAccountRepository;
import com.kryptoeuro.accountmapper.domain.AuthorisationType;
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

	public EthereumAccount storeNewAccount(String address, String ownerId, AuthorisationType authorisationType) {
		try {
			validateAccountStoring(address, ownerId);
			EthereumAccount account = EthereumAccount.builder()
					.ownerId(ownerId)
					.address(address)
					.activated(false)
					.authorisationType(authorisationType)
					.build();
			ethereumAccountRepository.save(account);
			return account;
		} catch (CannotStoreAccountException e) {
			throw e;
		} catch (Exception e) {
			throw new CannotStoreAccountException("Crashed while storing new account", e.getCause());
		}
	}

	public void markActivated(EthereumAccount account,String txHash) {
		try {
			account.setActivated(true);
			account.setTransactionHash(txHash);
			ethereumAccountRepository.save(account);
		} catch (Exception e) {
			throw new CannotStoreAccountException("Crashed while activating new account", e.getCause());
		}
	}

	private void validateAccountStoring(String address, String ownerId) {
		if (address == null) throw new CannotStoreAccountException("Address undefined");
		if (ownerId == null) throw new CannotStoreAccountException("OwnerId undefined");
		//better be solved by domain object uniqueness constraint
		if (getAccountsByAccountAddress(address, true).size() > 0)
			throw new CannotStoreAccountException("Account already in system");

	}

	public List<EthereumAccount> getAccountsByOwnerId(String ownerId) {
		return ethereumAccountRepository.findByOwnerId(ownerId);
	}

	public List<EthereumAccount> getAccountsByAccountAddress(String accountAddress) {
		return ethereumAccountRepository.findByAddress(accountAddress);
	}

	public List<EthereumAccount> getAccountsByAccountAddress(String accountAddress, boolean activated) {
		return ethereumAccountRepository.findByAddressAndActivated(accountAddress, activated);
	}

	public List<EthereumAccount> getAllAccounts() {
		return ethereumAccountRepository.findAll();
	}

	public void removeAccountById(Long id) {
		ethereumAccountRepository.delete(id);
	}
}
