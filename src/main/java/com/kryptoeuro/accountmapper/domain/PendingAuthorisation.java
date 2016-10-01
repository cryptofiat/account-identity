package com.kryptoeuro.accountmapper.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingAuthorisation {
	@Id
	private UUID authIdentifier; //binary(16) in db
	@Enumerated(EnumType.STRING)
	private AuthorisationType type;
	private String address;
	private String serialisedMobileIdSession;
}
