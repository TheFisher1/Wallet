package bg.sofia.uni.fmi.mjt.server.datastore;

import java.nio.channels.SelectionKey;
import java.util.LinkedHashMap;
import java.util.Map;

public class SessionStorage implements Storage<SelectionKey, String> {

    private final Map<SelectionKey, String> userSessions;

    public SessionStorage(Map<SelectionKey, String> userSessions) {
        this.userSessions = new LinkedHashMap<>();
        this.userSessions.putAll(userSessions);
    }

    @Override
    public void add(SelectionKey key, String value) {
        userSessions.put(key, value);
    }

    @Override
    public String get(SelectionKey key) {
        return userSessions.get(key);
    }

    @Override
    public void remove(SelectionKey key) {
        userSessions.remove(key);
    }
}
