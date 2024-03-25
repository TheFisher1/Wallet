package bg.sofia.uni.fmi.mjt.server.access.cache;

import bg.sofia.uni.fmi.mjt.server.access.Subject;

import java.util.Map;

public interface Cache<K, V> extends Runnable, Subject {

    V get(K key);

    Map<K, V> getAll();
}
