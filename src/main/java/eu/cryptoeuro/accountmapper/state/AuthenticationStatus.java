package eu.cryptoeuro.accountmapper.state;

public enum AuthenticationStatus {
	LOGIN_PENDING,
	LOGIN_SUCCESS,
	LOGIN_FAILURE,
	LOGIN_EXPIRED,
	LOGIN_INVALID_SIGNATURE,
}
