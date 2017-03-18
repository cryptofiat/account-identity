package eu.cryptoeuro.accountmapper.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.cryptoeuro.accountmapper.BackupChallengeRepository;
import eu.cryptoeuro.accountmapper.KeyBackupRepository;
import eu.cryptoeuro.accountmapper.domain.KeyBackup;
import eu.cryptoeuro.accountmapper.command.BackupKey;
import eu.cryptoeuro.accountmapper.domain.BackupChallenge;

import java.util.List;

@Service
@Slf4j
public class BackupService {

	@Autowired
	AccountManagementService accountService;
	@Autowired
	EthereumService ethereumService;
	@Autowired
	BackupChallengeRepository challengeRepository;
	@Autowired
	KeyBackupRepository keyRepository;
	@Autowired
	WalletServerService wsService;

	public BackupChallenge lookupIdCode(long idCode) {
		List<BackupChallenge> challengeList = challengeRepository.findByIdCodeAndActive(Long.valueOf(idCode),true);
		return (challengeList.isEmpty()) ? null : challengeList.get(0);
        }

	public boolean challengeEncryptedMatch(String plaintext, String encrypted, boolean active) {
		return !challengeRepository.findByPlaintextAndEncryptedAndActive(plaintext,encrypted,active).isEmpty();
        }

	public void storeChallenge(Long idCode, String plaintext, String encrypted) {
		// Check if an existing challenge needs to be inactivated
		List<BackupChallenge> challengeList = challengeRepository.findByIdCodeAndActive(idCode,true);
		challengeList.forEach( (existingChallenge) -> {
			existingChallenge.setActive(false);
			challengeRepository.save(existingChallenge);
		});
		BackupChallenge challenge = BackupChallenge.builder()
					.idCode(idCode)
					.plaintext(plaintext)
					.encrypted(encrypted)
					.active(Boolean.valueOf(true))
					.build();
		challengeRepository.save(challenge);
	}
	public boolean challengeExists(String challenge, boolean active) {
		return !challengeRepository.findByPlaintextAndActive(challenge,active).isEmpty();
        }

	public List<KeyBackup> getKeys(String challenge, boolean active) {
		return keyRepository.findByChallengeAndActive(challenge,active);
        }

	public void storeAnyNewKeys(String challenge, List<BackupKey> keys) {
		keys.forEach( (key) -> {
			List<KeyBackup> storedKeyList = keyRepository.findByChallengeAndAddress(challenge,key.getAddress());
			if (storedKeyList.isEmpty()) {
				KeyBackup storedKey = KeyBackup.builder()
							.challenge(challenge)
							.address(key.getAddress())
							.keyEnc(key.getKeyEnc())
							.active(key.getActive())
							.build();
				keyRepository.save(storedKey);
			} else {
				KeyBackup storedKey = storedKeyList.get(0);
				storedKey.setActive(key.getActive());
				storedKey.setKeyEnc(key.getKeyEnc());
				keyRepository.save(storedKey);
			}
		});
        }

}
