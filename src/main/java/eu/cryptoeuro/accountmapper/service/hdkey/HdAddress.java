package eu.cryptoeuro.accountmapper.service.hdkey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HdAddress {
    private Long index;
    private String address;
}
