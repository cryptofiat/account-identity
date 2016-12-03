package com.kryptoeuro.accountmapper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ethereum.crypto.ECKey;

import com.kryptoeuro.accountmapper.domain.Escrow;
import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.domain.AuthorisationType;
import com.kryptoeuro.accountmapper.response.AccountActivationResponse;
import com.kryptoeuro.accountmapper.EscrowRepository;
import com.kryptoeuro.accountmapper.security.EncryptionUtils;

import java.util.List;

@Service
@Slf4j
public class EscrowService {

	@Autowired
	AccountManagementService accountService;
	@Autowired
	EthereumService ethereumService;
	@Autowired
	EscrowRepository escrowRepository;
	@Autowired
	EncryptionUtils encUtils;

	private Escrow getExistingEscrow(long idCode) {
		List<Escrow> escrowList = escrowRepository.findByIdCodeAndCleared(Long.valueOf(idCode),false);
		return (escrowList.isEmpty()) ? null : escrowList.get(0);
        }

	public Escrow createEscrowKey(long idCode) {
		ECKey key = new ECKey();	
		Escrow escrow = Escrow.builder()
				.privateKey(encUtils.encrypt(ethereumService.hex(key.getPrivKeyBytes())))
				.address(ethereumService.hex(key.getAddress()))
				.idCode(idCode)
				.build();

		return escrow;
	};

	public EthereumAccount approveEscrowAccountForId(long idCode) {
		Escrow escrow = createEscrowKey(idCode);
		EthereumAccount ethAccount = accountService.storeNewAccount(escrow.getAddress(), String.valueOf(escrow.getIdCode()), AuthorisationType.ESCROW);
		String txHash = activateEthereumAccount(escrow.address);
		//String txHash = new String("0x123456-stub");
		accountService.markActivated(ethAccount, txHash);
		escrowRepository.save(escrow);
		return ethAccount;
	}
}
