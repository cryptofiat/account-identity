package com.kryptoeuro.accountmapper.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No active backup challenge stored for this ID code. Use PUT to add one.")
public class BackupNotFoundException extends RuntimeException {
}
