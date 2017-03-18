package eu.cryptoeuro.accountmapper.rest;

import eu.cryptoeuro.accountmapper.domain.BackupChallenge;
import eu.cryptoeuro.accountmapper.domain.KeyBackup;
import eu.cryptoeuro.accountmapper.service.BackupService;
import eu.cryptoeuro.accountmapper.service.LdapService;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import eu.cryptoeuro.accountmapper.error.LdapNotFoundException;
import eu.cryptoeuro.accountmapper.error.BackupNotFoundException;
import eu.cryptoeuro.accountmapper.error.BadBackupEncryptionException;

import eu.cryptoeuro.accountmapper.response.BackupChallengeResponse;
import eu.cryptoeuro.accountmapper.command.BackupKeysDao;
import eu.cryptoeuro.accountmapper.command.BackupChallengeCommand;
import eu.cryptoeuro.accountmapper.service.AccountManagementService;

@RestController
@RequestMapping("/v1/backup")
@Slf4j
@CrossOrigin(origins = "*")
public class BackupController {

	@Autowired
	LdapService ldapService;
	@Autowired
	BackupService backupService;
	@Autowired
	AccountManagementService accountService;

	@RequestMapping(method = RequestMethod.GET, value = "challenge")
	public ResponseEntity<BackupChallengeResponse> getChallenge(@RequestParam("idCode") @Valid long idCode) throws IOException {
		if (ldapService.lookupIdCode(idCode) == null) { throw new LdapNotFoundException(); };
		BackupChallenge challenge = backupService.lookupIdCode(idCode);
		if (challenge == null) { throw new BackupNotFoundException(); };

		return new ResponseEntity<BackupChallengeResponse>(BackupChallengeResponse.builder()
							.encrypted(challenge.getEncrypted())
							.build(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "keys",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<BackupKeysDao> syncKeys(@RequestBody @Valid BackupKeysDao keysDao) throws IOException {

		if (!backupService.challengeExists(keysDao.getChallenge(),true)) { throw new BackupNotFoundException(); };
		backupService.storeAnyNewKeys(keysDao.getChallenge(),keysDao.getKeys());

		List<KeyBackup> newKeys = backupService.getKeys(keysDao.getChallenge(),true);
		return new ResponseEntity<BackupKeysDao>(backupResponseFromKeys(newKeys,keysDao.getChallenge()), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "challenge",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<BackupChallengeResponse> tryEncryptionMatch(@RequestBody @Valid BackupChallengeResponse challengeCom) throws IOException {

		// if encrypted doesn't match then throw exception otherwise return input
		if (!backupService.challengeEncryptedMatch(challengeCom.getPlaintext(),challengeCom.getEncrypted(),true)) { 
			throw new BadBackupEncryptionException(); 
		};
		return new ResponseEntity<BackupChallengeResponse>(challengeCom, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "challenge",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<BackupChallengeCommand> setChallenge(@RequestBody @Valid BackupChallengeCommand challengeCom) throws IOException {

		if (ldapService.lookupIdCode(Long.valueOf(challengeCom.getIdCode())) == null) { throw new LdapNotFoundException(); };

		// if encrypted doesn't match then throw exception otherwise return input
		if (backupService.lookupIdCode(Long.valueOf(challengeCom.getIdCode())) != null && !backupService.challengeEncryptedMatch(challengeCom.getPlaintext(),challengeCom.getEncrypted(),true)) {
                        throw new BadBackupEncryptionException();
                };

		backupService.storeChallenge(challengeCom.getIdCode(),challengeCom.getPlaintext(),challengeCom.getNewEncrypted()); 
		return new ResponseEntity<BackupChallengeCommand>(challengeCom, HttpStatus.OK);
	}


	private BackupKeysDao backupResponseFromKeys(List<KeyBackup> keys, String challenge) {


		//BackupKeysDao response = new  BackupKeysDao(challenge);
		BackupKeysDao response = BackupKeysDao.builder().challenge(challenge).build();
		response.fromKeyList(keys);
		return response;
	}
}
