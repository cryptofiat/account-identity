package eu.cryptoeuro.accountmapper.response;

import eu.cryptoeuro.accountmapper.domain.EthereumAccount;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class AccountsResponse {

	List<Account> accounts;


	public static AccountsResponse fromEthereumAccounts(List<EthereumAccount> ethereumAccounts) {
		List<Account> accounts = new ArrayList();
		for (EthereumAccount accountDomain : ethereumAccounts) {
			;
			accounts.add(Account.builder()
							.id(accountDomain.getId())
							.address(accountDomain.getAddress())
							.ownerId(replaceLastFour(accountDomain.getOwnerId()))
							.activated(accountDomain.isActivated())
							.authorisationType(accountDomain.getAuthorisationType().name())
							.build()
			);
		}
		return AccountsResponse.builder().accounts(accounts).build();
	}

	private static String replaceLastFour(String s) {
		int length = s.length();
		if (length < 4) return s;
		return s.substring(0, length - 4) + "****";
	}
}
