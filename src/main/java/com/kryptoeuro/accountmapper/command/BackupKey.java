package com.kryptoeuro.accountmapper.command;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.kryptoeuro.accountmapper.domain.KeyBackup;

import java.util.List;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupKey {
/*
	public Key(String _address, String _key) {
		this.address = _address;
		this.key = _key;
	}
*/
	@NotNull
	@Size(min=42, max=42) // 160-bit address in hex + 0x beginning
	String address;
	@NotNull
	@Size(min=44, max=44) // 32 bytes AES encoded in base 64
	String keyEnc;
	Boolean active;
}
