package eu.cryptoeuro.accountmapper.service.hdkey

import spock.lang.Specification

class HDKeySpec extends Specification {

    def "DeriveAddressFromKey: 1"() {
        when:
        HdAddress address = HDKey.deriveAddressFromKey(
                "xpub6CM8PoQfptuaSd9xHY4DRNvXiGhDeEAHqfgBdhAhFFjdEzfZz61qE8fhbirgAL4c8bFJL91vbwafckpBoa2vPUSb3BCGAK9YF5jRiYpcdX9",
                1
        )
        then:
        address.address == "0x9d4276fc7e811fc57136702b8c730559084dcd64"
    }

    def "DeriveAddressFromKey: 2"() {
        when:
        HdAddress address = HDKey.deriveAddressFromKey(
                "xprv9s21ZrQH143K2JF8RafpqtKiTbsbaxEeUaMnNHsm5o6wCW3z8ySyH4UxFVSfZ8n7ESu7fgir8imbZKLYVBxFPND1pniTZ81vKfd45EHKX73",
                1
        )
        then:
        address.address == "0xe635bc083d013ced2a0f00175485d1b18c93f00f"
    }

}
