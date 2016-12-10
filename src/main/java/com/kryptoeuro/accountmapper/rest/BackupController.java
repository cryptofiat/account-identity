package com.kryptoeuro.accountmapper.rest;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import com.kryptoeuro.accountmapper.error.LdapNotFoundException;
import com.kryptoeuro.accountmapper.error.BackupNotFoundException;
import com.kryptoeuro.accountmapper.error.BadBackupEncryptionException;
import com.kryptoeuro.accountmapper.error.HasActiveAccountException;

import com.kryptoeuro.accountmapper.response.BackupChallengeResponse;
import com.kryptoeuro.accountmapper.command.BackupKeysDao;
import com.kryptoeuro.accountmapper.command.BackupChallengeCommand;
import com.kryptoeuro.accountmapper.domain.BackupChallenge;
import com.kryptoeuro.accountmapper.domain.KeyBackup;
import com.kryptoeuro.accountmapper.service.LdapService;
import com.kryptoeuro.accountmapper.service.AccountManagementService;
import com.kryptoeuro.accountmapper.service.BackupService;

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
							.plaintext(challenge.getPlaintext())
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
