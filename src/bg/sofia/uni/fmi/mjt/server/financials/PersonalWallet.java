package bg.sofia.uni.fmi.mjt.server.financials;

import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmptyMarketException;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PersonalWallet {
    private Map<String, Double> currencies;

    public PersonalWallet(Map<String, Double> currencies) {
        this.currencies = new LinkedHashMap<>();
        this.currencies.putAll(currencies);
    }

    public PersonalWallet() {
        this.currencies = new LinkedHashMap<>();
    }

    @Override
    public String toString() {
        return currencies.toString();
    }

    public void acquireAsset(double money,
                             String assetId,
                             Map<String, Currency> assets) {
        if (assets == null || assets.isEmpty()) {
            throw new EmptyMarketException("there was an error");
        }

        Double amount = money / assets.get(assetId).priceUsd();

        if (currencies.get(assetId) != null) {
            amount += currencies.get(assetId);
        }

        currencies.put(assetId, amount);
    }

    public double sellAsset(String assetId, Map<String, Currency> assets) {
        double amount = currencies.get(assetId);
        currencies.remove(assetId);
        return amount * assets.get(assetId).priceUsd();
    }

    public Double get(String assetId) {
        return currencies.get(assetId);
    }

    public Map<String, Double> get() {
        return Collections.unmodifiableMap(currencies);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PersonalWallet personalWallet)) {
            return false;
        }

        return this.currencies.equals(personalWallet.currencies);
    }

    @Override
    public int hashCode() {
        return currencies.hashCode();
    }
}