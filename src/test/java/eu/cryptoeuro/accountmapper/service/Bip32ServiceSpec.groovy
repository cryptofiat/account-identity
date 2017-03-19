package eu.cryptoeuro.accountmapper.service

import spock.lang.Specification

class Bip32ServiceSpec extends Specification {

    Bip32Service service = new Bip32Service()

    def "generate master key from seed"() {
        given:
        String seed = "000102030405060708090a0b0c0d0e0f"
        when:
        String extendedKey = service.generateMasterKey(seed)
        then:
        extendedKey == "xprv2w77MNL1q1qdS6PHAH27ZKNuC1NJrfpnfBT37jcL25x8nMvpchB8hPLXSgaq4d7TMLapNrvkfzjHkCnZB9ZR8s34f9bsEuQ9zzraU81vC"
    }




    def "generate children from public key"() {
        given:
        String extendedPublicKey = "xprv9s21ZrQH143K2JF8RafpqtKiTbsbaxEeUaMnNHsm5o6wCW3z8ySyH4UxFVSfZ8n7ESu7fgir8imbZKLYVBxFPND1pniTZ81vKfd45EHKX73"
        int childNr = 1;
        when:

        String childKey = service.getChild(extendedPublicKey, childNr);

        then:
        true

    }

}
