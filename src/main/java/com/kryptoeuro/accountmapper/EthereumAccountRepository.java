package com.kryptoeuro.accountmapper;

import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import com.kryptoeuro.accountmapper.domain.AuthorisationType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EthereumAccountRepository extends CrudRepository<EthereumAccount,Long> {

    List<EthereumAccount> findByOwnerId(String ownerId);
    List<EthereumAccount> findByAddress(String accountAddress);

    List<EthereumAccount> findByAddressAndActivated(String accountAddress, boolean activated);
    List<EthereumAccount> findByOwnerIdAndActivated(String accountAddress, boolean activated);
    List<EthereumAccount> findByOwnerIdAndAuthorisationTypeNot(String accountAddress, AuthorisationType authType);
    List<EthereumAccount> findByOwnerIdAndActivatedAndAuthorisationTypeNot(String accountAddress, boolean activated, AuthorisationType authType);
    List<EthereumAccount> findAll();

    void delete(Long id);
}
