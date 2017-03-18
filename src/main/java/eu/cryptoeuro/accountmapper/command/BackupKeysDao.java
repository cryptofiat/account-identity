package eu.cryptoeuro.accountmapper.command;

import eu.cryptoeuro.accountmapper.domain.KeyBackup;
import lombok.Data;
import lombok.Builder;
import lombok.Singular;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.List;
import java.util.ArrayList;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupKeysDao {
/*
	public BackupKeysDao(String _challenge) {
		this.challenge  = _challenge;
	}
*/
	@NotNull
	@Size(min=32, max=32) // 24 bytes AES encoded in base 64
	String challenge; 
	@Singular List<BackupKey> keys;
	public void fromKeyList(List<KeyBackup> _keys) {
		// Overwriting  whatever builder might have created as an object
		keys = new ArrayList<BackupKey>();
		_keys.forEach( (key) -> {
			BackupKey xkey = BackupKey.builder()
						.address(key.getAddress())
						.keyEnc(key.getKeyEnc())
						.active(key.getActive())
						.build();
			keys.add(xkey);
		});
	}
}
