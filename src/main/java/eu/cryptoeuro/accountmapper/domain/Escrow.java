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
public class Escrow {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private Long idCode;
    private String address;
    //AES encrypted with local password
    private String privateKey;

    // set to true after being cleared
    private boolean cleared = false;
    // hash of the clearing transaction 
    private String clearingHash;
}
