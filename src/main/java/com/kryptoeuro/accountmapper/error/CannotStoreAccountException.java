package com.kryptoeuro.accountmapper.error;

public class CannotStoreAccountException extends Exception {

	public CannotStoreAccountException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotStoreAccountException(String message) {
		super(message);
	}
}
