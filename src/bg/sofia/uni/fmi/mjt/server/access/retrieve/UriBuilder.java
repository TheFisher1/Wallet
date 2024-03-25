package bg.sofia.uni.fmi.mjt.server.access.retrieve;

import bg.sofia.uni.fmi.mjt.server.exceptions.UnauthorisedApiUseException;
import bg.sofia.uni.fmi.mjt.server.utils.secure.AES;
import bg.sofia.uni.fmi.mjt.server.utils.secure.Hash;
import bg.sofia.uni.fmi.mjt.server.utils.secure.SecretKeyHandler;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;

public class UriBuilder {

    private static final String API_SCHEME = "https://";
    private static final String API_HOST =  "rest.coinapi.io/";
    private static final String API_APIKEY_AUTHORIZATION = "APIKEY-";

    private static final String API_KEY = "<INSERT YOUR API KEY HERE>";
    private static final String API_ENDPOINT = "/v1/assets";
    public URI buildUri() {
        String apiKey = loadApiKey();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(API_SCHEME);
        stringBuffer.append(API_HOST);
        stringBuffer.append(API_APIKEY_AUTHORIZATION);
        stringBuffer.append(apiKey);
        stringBuffer.append(API_ENDPOINT);

        return URI.create(stringBuffer.toString());
    }

    private String loadApiKey() {
        SecretKey secretKey = SecretKeyHandler.loadSecretKey("secretKeyAPI.txt").get();
        Hash hash = new AES(secretKey);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (FileInputStream fileInputStream = new FileInputStream("apiKey.txt")) {
            hash.decrypt(Base64.getDecoder().wrap(fileInputStream), byteArrayOutputStream);
        } catch (IOException e) {
            throw new UnauthorisedApiUseException(e.getMessage());
        }

        return byteArrayOutputStream.toString();
    }
}
