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
public class KeyBackup {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String challenge;
    // should check length .. starts 0x
    private String address;
    private String keyEnc;
    private Boolean active;
}
