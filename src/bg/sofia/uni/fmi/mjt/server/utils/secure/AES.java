package bg.sofia.uni.fmi.mjt.server.utils.secure;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class AES implements Hash {

    private static final int KILOBYTE = 1024;
    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final String KEY_FILE_PATH = "secret.key";
    private static final int KEY_SIZE_IN_BITS = 128;

    private final SecretKey secretKey;

    public AES(SecretKey secretKey) {
        try {
            cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.err.println(e.getMessage());
        }

        this.secretKey = secretKey;
    }

    public static SecretKey generateSecretKey() {
        SecretKey secretKey;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
            keyGenerator.init(KEY_SIZE_IN_BITS);
            secretKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return secretKey;
    }

    private Cipher cipher;

    @Override
    public void encrypt(InputStream inputStream, OutputStream outputStream) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            //..
        }

        try (CipherOutputStream stream = new CipherOutputStream(outputStream, cipher)) {

            byte[] bytes = new byte[KILOBYTE];
            int bytesRead;

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                stream.write(bytes, 0, bytesRead);
            }
            stream.flush();
        } catch (IOException e) {
            //..
        }
    }

    @Override
    public void decrypt(InputStream inputStream, OutputStream outputStream) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e.getCause());
        }
        try (CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {

            byte[] buffer = new byte[KILOBYTE];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }
            cipherOutputStream.flush();

        } catch (IOException e) {
            //..
        }
    }
}