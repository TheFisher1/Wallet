package bg.sofia.uni.fmi.mjt.server.datastore;

public interface Storage<K, V> {

    void add(K key, V value);

    V get(K key);

    void remove(K toBeRemoved);
}
