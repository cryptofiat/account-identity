package eu.cryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class WalletServerHistoryResponse {
	private String id;
	private String targetAccount;
	private String sourceAccount;
	private long amount;
	private long timestamp;
}
