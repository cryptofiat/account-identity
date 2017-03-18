package eu.cryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletServerAccountResponse {
	private String address;
	private boolean approved;
	private boolean closed;
	private boolean frozen;
	private long nonce;
	private long balance;
}
