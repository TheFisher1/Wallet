package bg.sofia.uni.fmi.mjt.server.utils;

import bg.sofia.uni.fmi.mjt.server.exceptions.FileNotAvailableException;
import bg.sofia.uni.fmi.mjt.server.financials.PersonalWallet;
import bg.sofia.uni.fmi.mjt.server.utils.secure.AES;
import bg.sofia.uni.fmi.mjt.server.utils.secure.Hash;
import bg.sofia.uni.fmi.mjt.server.financials.Transaction;
import bg.sofia.uni.fmi.mjt.server.user.User;
import bg.sofia.uni.fmi.mjt.server.utils.secure.SecretKeyHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TextProcessor {
    private final Hash hash;
    private static final Gson GSON = new Gson();
    private static final String DELIMITER = ";;";

    private static final String SECRET_KEY_FILE = "secretKey.txt";

    public TextProcessor() {
        Optional<SecretKey> key = SecretKeyHandler.loadSecretKey(SECRET_KEY_FILE);

        if (key.isEmpty()) {
            SecretKey secretKey = AES.generateSecretKey();
            hash = new AES(secretKey);
            SecretKeyHandler.persistSecretKey(secretKey, SECRET_KEY_FILE);
        } else {
            hash = new AES(key.get());
        }

    }

    public void saveUsers(Writer writer, Collection<User> users) {

        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

            for (User user : users) {
                bufferedWriter.write((user.getName()));
                bufferedWriter.write(";;");
                ByteArrayOutputStream encodedPassword = new ByteArrayOutputStream();

                ByteArrayInputStream passwordByteArrInputStream = new ByteArrayInputStream(
                                                                  user.getPassword()
                                                                      .getBytes(StandardCharsets.UTF_8)
                );

                hash.encrypt(passwordByteArrInputStream, encodedPassword);

                bufferedWriter.write(encodeBase64(encodedPassword.toByteArray()));
                bufferedWriter.write(DELIMITER);
                bufferedWriter.write(user.getBalance() + "");
                bufferedWriter.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new FileNotAvailableException(e.getMessage());
        }
    }

    public Collection<User> loadUsers(Reader reader) {
        List<User> userList = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(";;");
                String username = parts[0];
                String encryptedPassword = parts[1];
                String balance = parts[2];

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    encryptedPassword.getBytes(StandardCharsets.UTF_8));
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                hash.decrypt(
                    Base64.getDecoder().wrap(new ByteArrayInputStream(
                        encryptedPassword.getBytes(StandardCharsets.UTF_8))),
                    outputStream);
                double balanceInDouble = Double.parseDouble(balance);
                User loadedUser = new User(username,
                    outputStream.toString(StandardCharsets.UTF_8), balanceInDouble);

                userList.add(loadedUser);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return userList;
    }

    public void addUser(User user, Writer writer) throws IOException {

        writer.write(user.getName());
        writer.write(DELIMITER);
        ByteArrayOutputStream encodedPassword = new ByteArrayOutputStream();

        ByteArrayInputStream passwordByteArrInputStream =
            new ByteArrayInputStream(user.getPassword().getBytes(StandardCharsets.UTF_8));
        hash.encrypt(passwordByteArrInputStream, encodedPassword);

        writer.write(encodeBase64(encodedPassword.toByteArray()));
        writer.write(DELIMITER);
        writer.write(user.getBalance() + "");
        writer.write(System.lineSeparator());
        writer.flush();

    }

    private String encodeBase64(byte[] outputStream) {
        return Base64.getEncoder().encodeToString(outputStream);
    }

    public void savePersonalAssets(Writer writer, Map<String, PersonalWallet> personalWallets) {

        try (Writer bufferedwriter = new BufferedWriter(writer)) {
            bufferedwriter.write(GSON.toJson(personalWallets));
            bufferedwriter.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public Map<String, PersonalWallet> loadPersonalAssets(Reader reader) {
        Map<String, PersonalWallet> personalAssets = new LinkedHashMap<>();

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line = bufferedReader.readLine();

            personalAssets = GSON.fromJson(line,
                new TypeToken<Map<String, PersonalWallet>>() { }.getType());

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        return personalAssets;
    }

    public Map<String, Map<String, Transaction>> loadTransactionHistory(Reader reader) throws IOException {

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine();

            return GSON.fromJson(line,
                new TypeToken<Map<String, Map<String, Transaction>>>() { }.getType());
        }

    }

    public void saveTransactionHistory(Writer writer, Map<String, Map<String, Transaction>> transactionStorage) {

        try (BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            bufferedWriter.write(GSON.toJson(transactionStorage));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

}

