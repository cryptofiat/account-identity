package eu.cryptoeuro.accountmapper.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.FORBIDDEN, reason="Search too broad. Perhaps search string too short.")
public class SearchTooBroadException extends RuntimeException {
}
