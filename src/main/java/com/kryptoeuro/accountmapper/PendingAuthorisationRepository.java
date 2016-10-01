package com.kryptoeuro.accountmapper;

import com.kryptoeuro.accountmapper.domain.PendingAuthorisation;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PendingAuthorisationRepository extends CrudRepository<PendingAuthorisation, UUID> {

	PendingAuthorisation save(PendingAuthorisation pendingAuthorisation);

	PendingAuthorisation findByAuthIdentifier(UUID authIdentifier);

	void delete(PendingAuthorisation pendingAuthorisation);
}
