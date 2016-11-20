package com.kryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LdapResponse {
	private Long idCode;
	private String firstName;
	private String lastName;
}
