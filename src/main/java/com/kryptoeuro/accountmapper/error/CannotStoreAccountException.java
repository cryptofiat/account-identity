package com.kryptoeuro.accountmapper.error;

public class CannotStoreAccountException extends RuntimeException {

	public CannotStoreAccountException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotStoreAccountException(String message) {
		super(message);
	}
}
