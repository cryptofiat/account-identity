package com.kryptoeuro.accountmapper.domain;

import static org.ethereum.crypto.HashUtil.sha3;

import org.ethereum.crypto.ECKey;
import org.spongycastle.util.encoders.Hex;

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
	// TODO: Better types for address and public key
	// private String publicKey;
	private String serialisedMobileIdSession;
	private String bankTransferPaymentReference;
/*
	public ECKey getPublicKeyParsedFrom() {
		return ECKey.fromPublicOnly(Hex.decode(publicKey));
	}
*/
	public byte[] getChallengeForEthereumAccountHolder() {
		return authIdentifier.toString().getBytes();
	}

	/** @param signature	a DER-encoded ECDSA signature of the Ethereum account holder */
	public boolean verifyChallengeSignedByEthereumAccountHolder(byte[] signature) {
		byte[] signedHash = sha3(getChallengeForEthereumAccountHolder());
		return true;
		//TODO: this should be possible with recoverAddressFromSignature()
//		return getPublicKeyParsedFrom().verify(signedHash, signature);
	}
}
