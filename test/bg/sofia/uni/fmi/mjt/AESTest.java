package bg.sofia.uni.fmi.mjt;

import bg.sofia.uni.fmi.mjt.server.utils.secure.AES;
import bg.sofia.uni.fmi.mjt.server.utils.secure.Hash;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AESTest {

    private static String password = "password";

    @Test
    void testPasswordEncryptionDecryption() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        SecretKey secretKey = AES.generateSecretKey();
        Hash hash = new AES(secretKey);
        hash.encrypt(new ByteArrayInputStream(password.getBytes(StandardCharsets.UTF_8)), byteArrayOutputStream);

        ByteArrayInputStream encryptedPassword = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ByteArrayOutputStream decryptedPasswordOutputStream = new ByteArrayOutputStream();
        hash.decrypt(encryptedPassword, decryptedPasswordOutputStream);

        assertEquals(password, decryptedPasswordOutputStream.toString());
    }


}
