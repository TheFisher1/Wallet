package bg.sofia.uni.fmi.mjt.server.access;

public interface Subject {
    void addObserver(CurrencyObserver observer);

    void removeObserver(CurrencyObserver observer);

    void notifyObservers();
}
