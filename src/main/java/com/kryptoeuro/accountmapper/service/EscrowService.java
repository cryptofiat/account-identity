package com.kryptoeuro.accountmapper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ethereum.crypto.ECKey;
import java.io.IOException;

import com.kryptoeuro.accountmapper.domain.Escrow;
import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.domain.AuthorisationType;
import com.kryptoeuro.accountmapper.response.AccountActivationResponse;
import com.kryptoeuro.accountmapper.EscrowRepository;
import com.kryptoeuro.accountmapper.security.EncryptionUtils;
import com.kryptoeuro.accountmapper.response.WalletServerAccountResponse;
import com.kryptoeuro.accountmapper.response.EscrowTransfer;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

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
	@Autowired
	WalletServerService wsService;

	public Escrow getExistingEscrow(long idCode) {
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

	public EthereumAccount approveEscrowAccountForId(long idCode) throws IOException {
		Escrow escrow = createEscrowKey(idCode);
		EthereumAccount ethAccount = accountService.storeNewAccount(escrow.getAddress(), String.valueOf(escrow.getIdCode()), AuthorisationType.ESCROW);
		String txHash = ethereumService.activateEthereumAccount(escrow.getAddress());
		accountService.markActivated(ethAccount, txHash);
		escrowRepository.save(escrow);
		return ethAccount;
	}
	public  List<EscrowTransfer> clearAllToAddress(long idCode, String address) throws IOException {
		Escrow escrow;
		List<EscrowTransfer> etxs = new ArrayList<EscrowTransfer>();

		// Need to wait until the new account approval has been mined
		// doesn't work for multiple, because of eth nonce - need to create queue
		// assuming that this is always called  after Account Approval tx
		int nonceIncrement = 1;

		while ( (escrow = getExistingEscrow(idCode)) != null) {
			EscrowTransfer transfer = clearToAddress(escrow, address,nonceIncrement);
			if (transfer != null) {
				nonceIncrement++;
				etxs.add(transfer);
				escrow.setClearingHash(transfer.getTransactionHash());
			}
			escrow.setCleared(true);
			escrowRepository.save(escrow);
			// here should copy references over too
		}
		return etxs;
	}
	public EscrowTransfer clearToAddress(Escrow escrow, String address, int nonceIncrement) throws IOException {

		WalletServerAccountResponse addrDetails = wsService.getAccount(escrow.getAddress()); 
		long bal = addrDetails.getBalance();
		if (bal > 0) {
			log.info("Move "+String.valueOf(addrDetails.getBalance())+" to "+ address + " sign with " + encUtils.decrypt(escrow.getPrivateKey()));

			String txHash = ethereumService.sendBalance(address,encUtils.decrypt(escrow.getPrivateKey()), nonceIncrement);
			EscrowTransfer escrowTransfer = EscrowTransfer.builder()
				.amount(bal)
				.transactionHash(txHash)
				.timestamp(new Date().getTime())
				.build();
			return escrowTransfer;
		} else {
			return null;
		}
	}
}
