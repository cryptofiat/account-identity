package eu.cryptoeuro.accountmapper.rest;

import eu.cryptoeuro.accountmapper.service.hdkey.HDKey;
import eu.cryptoeuro.accountmapper.service.hdkey.HdAddress;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/v1")
@Slf4j
@CrossOrigin(origins = "*")
public class HdKeyController {

    @ApiOperation(value = "Get user address")
    @RequestMapping(method = GET, value = "/address")
    public HdAddress listAccounts(
            @RequestParam(name = "idCode", required = true) String idCode
    ) {
        String extendedPublicKey = "xprv9s21ZrQH143K2JF8RafpqtKiTbsbaxEeUaMnNHsm5o6wCW3z8ySyH4UxFVSfZ8n7ESu7fgir8imbZKLYVBxFPND1pniTZ81vKfd45EHKX73";

        return HDKey.deriveAddressFromKey(extendedPublicKey, 1);
    }

}
