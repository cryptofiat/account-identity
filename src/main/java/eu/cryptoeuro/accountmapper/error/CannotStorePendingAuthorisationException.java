package eu.cryptoeuro.accountmapper.error;

public class CannotStorePendingAuthorisationException extends RuntimeException {

	public CannotStorePendingAuthorisationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotStorePendingAuthorisationException(String message) {
		super(message);
	}
}
