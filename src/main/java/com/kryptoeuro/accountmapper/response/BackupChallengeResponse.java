package com.kryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupChallengeResponse {
	private String plaintext;
	private String encrypted;
}
