package eu.cryptoeuro.accountmapper.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthereumAccount {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String ownerId;
    private String address;
    private String transactionHash;
    private boolean activated;
    @Enumerated(EnumType.STRING)
    private AuthorisationType authorisationType;
}
