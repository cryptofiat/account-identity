package eu.cryptoeuro.accountmapper.command;

import org.spongycastle.util.encoders.Hex;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PollCommand {
	@NotNull
	UUID authIdentifier;

	String signature; // TODO: Document: DER-encoded format, in hex; also refactor to better types.

    public byte[] getSignatureParsedForm() {
        return Hex.decode(signature);
    }
}
