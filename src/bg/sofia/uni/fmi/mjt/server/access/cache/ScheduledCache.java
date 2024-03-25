package bg.sofia.uni.fmi.mjt.server.access.cache;

import bg.sofia.uni.fmi.mjt.server.access.CurrencyObserver;
import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.access.retrieve.CurrencyRetriever;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScheduledCache implements Cache<String, Currency> {
    private static final int LIMIT = 50;
    private List<Currency> currencyList;
    Map<String, Currency> currencyMap;
    private final List<CurrencyObserver> observers = new ArrayList<>();
    private HttpClient httpClient;

    public ScheduledCache(List<Currency> currencies) {
        currencyList = currencies;
        currencyMap = currencyList.stream().collect(Collectors.toMap(Currency::assetId, Function.identity()));
        this.httpClient = HttpClient.newBuilder().build();
    }

    public ScheduledCache() {
        currencyList = new ArrayList<>();
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public Currency get(String assetId) {
        return currencyMap.get(assetId);
    }

    @Override
    public Map<String, Currency> getAll() {
        return currencyMap;
    }

    @Override
    public void run() {
        this.currencyList = CurrencyRetriever.retrieveResources(httpClient);
        currencyMap = currencyList.parallelStream()
            .filter(currency -> currency.isCrypto() == 1
                && currency.priceUsd() != 0.0)
            .limit(LIMIT)
            .collect(Collectors.toMap(Currency::assetId, Function.identity()));
        notifyObservers();
    }

    @Override
    public void addObserver(CurrencyObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(CurrencyObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (var observer : observers) {
            observer.update(currencyMap);
        }
    }
}
