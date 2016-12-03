package com.kryptoeuro.accountmapper;

import com.kryptoeuro.accountmapper.response.LdapResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LdapResponseRepository extends CrudRepository<LdapResponse,Long> {

    List<LdapResponse> findByIdCode(Long idCode);

    void delete(Long id);
}
