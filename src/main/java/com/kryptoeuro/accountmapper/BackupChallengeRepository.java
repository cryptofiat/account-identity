package com.kryptoeuro.accountmapper;

import com.kryptoeuro.accountmapper.domain.BackupChallenge;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BackupChallengeRepository extends CrudRepository<BackupChallenge,Long> {

    List<BackupChallenge> findByIdCode(Long idCode);
    List<BackupChallenge> findByIdCodeAndActive(Long idCode, boolean active);
    List<BackupChallenge> findByEncryptedAndActive(String encrypted, boolean active);
    List<BackupChallenge> findByPlaintextAndEncryptedAndActive(String plaintext, String encrypted, boolean active);

    void delete(Long id);
}
