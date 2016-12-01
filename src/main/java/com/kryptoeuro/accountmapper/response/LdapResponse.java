package com.kryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LdapResponse {
        @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	private Long idCode;
	private String firstName;
	private String lastName;

}
