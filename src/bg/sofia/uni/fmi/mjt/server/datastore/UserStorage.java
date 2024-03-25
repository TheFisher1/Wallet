package bg.sofia.uni.fmi.mjt.server.datastore;

import bg.sofia.uni.fmi.mjt.server.exceptions.FileNotAvailableException;
import bg.sofia.uni.fmi.mjt.server.exceptions.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.user.User;
import bg.sofia.uni.fmi.mjt.server.utils.TextProcessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UserStorage {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final TextProcessor textProcessor;
    private String usersFile;

    public UserStorage(String usersFile) {
        textProcessor = new TextProcessor();
        this.usersFile = usersFile;
        try {
            FileReader fileReader = new FileReader(usersFile);
            this.users.putAll(load(fileReader));
        } catch (IOException e) {
            this.users.putAll(new LinkedHashMap<>());
        }

    }

    public UserStorage(Map<String, User> users) {
        textProcessor = new TextProcessor();
        this.users.putAll(users);
    }

    public void save(Writer writer) {
        textProcessor.saveUsers(writer, users.values());
    }

    public void save() throws IOException {
        save(new FileWriter(usersFile));
    }

    public Map<String, User> load(Reader reader) {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {

            Collection<User> users1 = textProcessor.loadUsers(bufferedReader);

            return users1.stream()
                         .collect(Collectors.toMap(
                            User::getName, Function.identity())
                        );

        } catch (IOException e) {
            return new HashMap<>();
        }

    }

    public void deposit(String username, double amount) throws UserNotFoundException, NegativeValueException {
        if (users.get(username) == null) {
            throw new UserNotFoundException("user with username: " + username + " could not be found");
        }

        User user = users.get(username);
        user.deposit(amount);
        users.put(username, user);
    }

    public void withdraw(String username, double amount)
        throws InsufficientBalanceException, UserNotFoundException, NegativeValueException {
        if (users.get(username) == null) {
            throw new UserNotFoundException("user with username " + username + " could not be found");
        }

        User user = users.get(username);
        user.withdraw(amount);
        users.put(username, user);
    }

    public void addUser(String key, User value) {
        users.put(key, value);
    }

    public void add(String key, User value) {

        if (usersFile != null) {
            try (FileWriter fileWriter = new FileWriter(usersFile, true)) {
                textProcessor.addUser(value, fileWriter);

            } catch (IOException e) {
                throw new FileNotAvailableException("there was problem opening the users file" , e);
            }

        }

        users.put(key, value);
    }

    public User get(String key) {
        return users.get(key);
    }

    public Collection<String> getKeys() {
        return Collections.unmodifiableCollection(users.keySet());
    }

    public Collection<User> getValues() {
        return Collections.unmodifiableCollection(users.values());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserStorage)) {
            return false;
        }

        return users.equals(((UserStorage) o).users);
    }

    @Override
    public int hashCode() {
        return users.hashCode();
    }

}


