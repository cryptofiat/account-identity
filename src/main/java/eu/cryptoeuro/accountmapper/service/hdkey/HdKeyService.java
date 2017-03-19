package eu.cryptoeuro.accountmapper.service.hdkey;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Slf4j
public class HdKeyService {

    private String bip32WorkingPath;

    @PostConstruct
    private void initialize() throws IOException, URISyntaxException {
        File bip32zip = new ClassPathResource("bip32implementation.zip").getFile();
        File tempDir = com.google.common.io.Files.createTempDir();
        this.bip32WorkingPath = tempDir.getAbsolutePath() + "/bip32implementation";
        unzipBip32Implementation(
                bip32zip,
                tempDir);
    }

    private void unzipBip32Implementation(File inputFile, File outputDirectory) throws IOException {
        java.util.zip.ZipFile zipFile = new ZipFile(inputFile);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputDirectory,  entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
            zipFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            zipFile.close();
        }
    }

    public HdAddress deriveAddress(String extendedPublicKey, Long index){
        String pwd = bip32WorkingPath;
        ProcessBuilder pb = (new ProcessBuilder())
                .directory(new File(pwd))
                .command("node", "generate-child.js", extendedPublicKey, index.toString());

        log.info("Running command in {}", pb.directory().getAbsolutePath());
        log.info("Command {}", pb.command().toString());
//        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        Process process = null;
        String processOutput = null;
        try {
            process = pb.start();
            processOutput = readProcess(process.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        log.info("Process output {}", processOutput);
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
