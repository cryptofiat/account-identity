package eu.cryptoeuro.accountmapper.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.UNAUTHORIZED, reason="Failed to match challenge encryption. Probably wrong password.")
public class BadBackupEncryptionException extends RuntimeException {
}
