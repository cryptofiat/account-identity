package com.kryptoeuro.accountmapper;

import com.kryptoeuro.accountmapper.domain.KeyBackup;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface KeyBackupRepository extends CrudRepository<KeyBackup,Long> {

    //List<BackupChallenge> findByIdCode(Long idCode);
    List<KeyBackup> findByChallengeAndActive(String challenge, boolean active);
    List<KeyBackup> findByChallengeAndAddress(String challenge, String address);

    void delete(Long id);
}
