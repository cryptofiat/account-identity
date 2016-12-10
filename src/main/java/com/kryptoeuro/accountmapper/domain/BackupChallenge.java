package com.kryptoeuro.accountmapper.domain;

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
public class BackupChallenge {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private Long idCode;
//    @Size(min=32, max=32) // 24 bytes base64 encoded - should validate encoding with regexp
    private String plaintext;
//    @Size(min=32, max=32) // 24 bytes base64 encoded - should validate encoding with regexp
    private String encrypted;
    private Boolean active;
}
