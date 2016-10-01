package com.kryptoeuro.accountmapper.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
    private boolean activated;
}
