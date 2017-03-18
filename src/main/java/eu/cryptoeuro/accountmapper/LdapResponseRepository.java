package eu.cryptoeuro.accountmapper;

import eu.cryptoeuro.accountmapper.response.LdapResponse;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LdapResponseRepository extends CrudRepository<LdapResponse,Long> {

    List<LdapResponse> findByIdCode(Long idCode);

    void delete(Long id);

    // cheap-o alternative to lucene or elasticsearch
    @Query("SELECT r FROM LdapResponse r WHERE "
		+ "r.firstName LIKE '%' || :searchString || '%' "
		+ "OR r.lastName LIKE '%' || :searchString || '%' "
		+ "OR r.idCode LIKE '%' || :searchString || '%' ")
    List<LdapResponse> searchLdapResponse(@Param("searchString") String searchString, Pageable pageable);

}
