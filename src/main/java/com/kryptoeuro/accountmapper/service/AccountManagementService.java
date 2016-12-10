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
	@Autowired
	EscrowService escrowService;
	@Autowired
	EthereumService ethService;

	public EthereumAccount storeNewAccount(String address, String ownerId, AuthorisationType authorisationType) {
		address = ethService.without0x(address);
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

	public boolean hasActivatedAccount(long ownerId) { return hasActivatedAccount(String.valueOf(ownerId)); }
	public boolean hasActivatedAccount(String ownerId) {
		
		List<EthereumAccount> approvedList = ethereumAccountRepository.findByOwnerIdAndActivated(ownerId,true);
		log.info("checked repo for " +ownerId+ " accounts and found "+String.valueOf(approvedList.size()));
		return !approvedList.isEmpty();
	}

	public List<EthereumAccount> getAccountsByOwnerIdActiveEscrow(String ownerId,boolean inactive, boolean escrow) {
		if (inactive && escrow) {
			return ethereumAccountRepository.findByOwnerId(ownerId);
		} else if (escrow) {
			return ethereumAccountRepository.findByOwnerIdAndActivated(ownerId,true);
		} else if (inactive) {
			return ethereumAccountRepository.findByOwnerIdAndAuthorisationTypeNot(ownerId,AuthorisationType.ESCROW);
		} else {
			return ethereumAccountRepository.findByOwnerIdAndActivatedAndAuthorisationTypeNot(ownerId,true,AuthorisationType.ESCROW);
		}
		
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


	public void deactivateAddress(String address) {
		//eg when clearing escrow
		List<EthereumAccount> accounts = getAccountsByAccountAddress(ethService.without0x(address));
		accounts.forEach((acc) -> {
			acc.setActivated(false);
			ethereumAccountRepository.save(acc);
		});
	}
}
