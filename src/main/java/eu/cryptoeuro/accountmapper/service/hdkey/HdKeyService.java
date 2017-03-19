package eu.cryptoeuro.accountmapper.service.hdkey;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class HdKeyService {

    public HdAddress deriveAddress(String extendedPublicKey, Long index) throws IOException, InterruptedException {
        String pwd = "/Users/jordan.valdma/Documents/Workspace/kryptoeuro/bip32exp";
        ProcessBuilder pb = (new ProcessBuilder())
                .directory(new File(pwd))
                .command("node", "generate-child.js", extendedPublicKey, index.toString());

//        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = pb.start();

        String processOutput = readProcess(process.getInputStream());
        log.info(processOutput);
        return deserializeAddress(processOutput);
    }

    private HdAddress deserializeAddress(String json) {
        HdAddress hdAddress = null;
        try {
            hdAddress = (new ObjectMapper()).readValue(json, HdAddress.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hdAddress;
    }

    private String readProcess(InputStream input) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining("\n"));
        }
    }

}
