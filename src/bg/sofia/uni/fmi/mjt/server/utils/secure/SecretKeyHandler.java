package bg.sofia.uni.fmi.mjt.server.utils.secure;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class SecretKeyHandler {
    public static Optional<SecretKey> loadSecretKey(String secretKeyFile) {
        Path key = Path.of(secretKeyFile);

        if (!Files.exists(key)) {
            return Optional.empty();
        }

        byte[] keyBytes = null;

        try {
            keyBytes = Files.readAllBytes(key);
        } catch (IOException e) {
            return Optional.empty();
        }

        return Optional.of(new SecretKeySpec(keyBytes, "AES"));
    }

    public static void persistSecretKey(SecretKey secretKey, String secretKeyFile) {
        byte[] keyBytes = secretKey.getEncoded();
        Path keyFilePath = Path.of(secretKeyFile);

        try {
            Files.write(keyFilePath, keyBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
