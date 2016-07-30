package com.kryptoeuro.accountmapper;

import com.kryptoeuro.accountmapper.domain.EthereumAccount;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EthereumAccountRepository extends CrudRepository<EthereumAccount,Long> {

    List<EthereumAccount> findByOwnerId(String ownerId);
}