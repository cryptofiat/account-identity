package com.kryptoeuro.accountmapper.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class EthereumAccount {
    @Id @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String ownerId;
    private String address;

    public EthereumAccount() {
    }

    public EthereumAccount(String ownerId, String address) {
        this.ownerId = ownerId;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getAddress() {
        return address;
    }
}
