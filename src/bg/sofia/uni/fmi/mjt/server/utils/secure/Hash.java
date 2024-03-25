package bg.sofia.uni.fmi.mjt.server.utils.secure;

import java.io.InputStream;
import java.io.OutputStream;

/*
    * Encryptable interface - object that can be encrypted and decrypted
    */

public interface Hash {

    void encrypt(InputStream inputStream, OutputStream outputStream);

    void decrypt(InputStream inputStream, OutputStream outputStream);

}
