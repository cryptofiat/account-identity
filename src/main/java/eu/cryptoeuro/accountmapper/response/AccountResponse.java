package eu.cryptoeuro.accountmapper.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class Account {
	private Long id;
	private String ownerId;
	private String address;
	private boolean activated;
	private String authorisationType;
}
