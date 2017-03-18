package eu.cryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowTransfer {
	private Long amount;
	private String transactionHash;
	private Long timestamp = new Long(new Date().getTime());
}
