package eu.cryptoeuro.accountmapper;

import eu.cryptoeuro.accountmapper.domain.PendingAuthorisation;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PendingAuthorisationRepository extends CrudRepository<PendingAuthorisation, UUID> {

	PendingAuthorisation save(PendingAuthorisation pendingAuthorisation);

	PendingAuthorisation findByAuthIdentifier(UUID authIdentifier);

	PendingAuthorisation findByBankTransferPaymentReference(String bankTransferPaymentReference);

	void delete(PendingAuthorisation pendingAuthorisation);
}
