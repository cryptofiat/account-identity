package eu.cryptoeuro.accountmapper.service.hdkey;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDDerivationException;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.ethereum.crypto.ECKey;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HDKey {
    public static HdAddress deriveAddressFromKey(String pubkey, int index) {
        while(index < 1 << 30){
            try {
                DeterministicKey master = DeterministicKey.deserializeB58(null, pubkey, MainNetParams.get());
                DeterministicKey child = HDKeyDerivation.deriveChildKey(master, index);
                ECKey childkey = ECKey.fromPublicOnly(child.getPubKey());

                HdAddress address = new HdAddress(
                        index,
                        "0x".concat(org.spongycastle.util.encoders.Hex.toHexString(childkey.getAddress()))
                );

                return address;
            } catch  (HDDerivationException e) {
                index++;
            }
        }
        throw new RuntimeException("Couldn't generate HD Key after max tries");
    }

}