package bg.sofia.uni.fmi.mjt.server.access;

import java.util.Map;

public interface Observer<T> {
    void update(Map<String, T> list);
}
