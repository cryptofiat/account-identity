package eu.cryptoeuro.accountmapper.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="This ID code already has associated active address.")
public class HasActiveAccountException extends RuntimeException {
}
