package com.kryptoeuro.accountmapper;

import com.kryptoeuro.accountmapper.domain.Escrow;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EscrowRepository extends CrudRepository<Escrow,Long> {

    List<Escrow> findByIdCode(Long idCode);
    List<Escrow> findByIdCodeAndCleared(Long idCode, boolean cleared);

    void delete(Long id);
}
