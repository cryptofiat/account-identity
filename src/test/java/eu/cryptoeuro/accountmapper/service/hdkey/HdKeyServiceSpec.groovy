package eu.cryptoeuro.accountmapper.service.hdkey

import spock.lang.Specification

class HdKeyServiceSpec extends Specification {

    HdKeyService service = new HdKeyService();

    def setup() {
        service.initialize()
    }

    def "generate child"() {

        given:
        String extendedPublicKey = "xprv9s21ZrQH143K2JF8RafpqtKiTbsbaxEeUaMnNHsm5o6wCW3z8ySyH4UxFVSfZ8n7ESu7fgir8imbZKLYVBxFPND1pniTZ81vKfd45EHKX73";
        Long index = new Long(1)

        when:
        HdAddress address = service.deriveAddress(extendedPublicKey, index)
        then:
        address.index == 1
        address.address == "0xe635bc083d013ced2a0f00175485d1b18c93f00f"

    }


}
