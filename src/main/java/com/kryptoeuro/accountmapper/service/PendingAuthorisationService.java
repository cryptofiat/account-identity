package com.kryptoeuro.accountmapper.service;


import com.codeborne.security.mobileid.MobileIDSession;
import com.kryptoeuro.accountmapper.PendingAuthorisationRepository;
import com.kryptoeuro.accountmapper.domain.AuthorisationType;
import com.kryptoeuro.accountmapper.domain.PendingAuthorisation;
import com.kryptoeuro.accountmapper.error.CannotStorePendingAuthorisationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class PendingAuthorisationService {

	@Autowired
	PendingAuthorisationRepository pendingAuthorisationRepository;

	@Autowired
	PaymentReferenceService paymentReferenceService;

	public PendingAuthorisation store(String accountAddress) {
		try {
			PendingAuthorisation newPendingAuthorisation = PendingAuthorisation.builder()
					.type(AuthorisationType.BANK_TRANSFER)
					.address(accountAddress)
					.authIdentifier(UUID.randomUUID())
					.build();
			return pendingAuthorisationRepository.save(newPendingAuthorisation);
		} catch (Exception e) {
			throw new CannotStorePendingAuthorisationException("Crashed while storing pending authorisation", e.getCause());
		}
	}

	public PendingAuthorisation store(String accountAddress, MobileIDSession mobileIDSession) {
		try {
			PendingAuthorisation newPendingAuthorisation = PendingAuthorisation.builder()
					.type(AuthorisationType.MOBILE_ID)
					.serialisedMobileIdSession(mobileIDSession.toString())
					.address(accountAddress)
					.authIdentifier(UUID.randomUUID())
					.build();
			return pendingAuthorisationRepository.save(newPendingAuthorisation);
		} catch (Exception e) {
			throw new CannotStorePendingAuthorisationException("Crashed while storing pending authorisation", e.getCause());
		}
	}

	public PendingAuthorisation addPaymentReferenceToPendingAuthorisation(PendingAuthorisation pendingAuthorisation) {
		String paymentReference = null;

		//Wow such duplicate check, much unique
		while (true) {
			paymentReference = paymentReferenceService.getRandomPaymentReference();
			if (findByPaymentReference(paymentReference) == null) break;
		}
		pendingAuthorisation.setBankTransferPaymentReference(paymentReference);
		return pendingAuthorisationRepository.save(pendingAuthorisation);
	}

	public PendingAuthorisation findByAuthIdentifier(UUID authIdentifier) {
		return pendingAuthorisationRepository.findByAuthIdentifier(authIdentifier);
	}

	public PendingAuthorisation findByPaymentReference(String paymentReference) {
		return pendingAuthorisationRepository.findByBankTransferPaymentReference(paymentReference);
	}

	public void expire(PendingAuthorisation pendingAuthorisation) {
		pendingAuthorisationRepository.delete(pendingAuthorisation);
	}
}
