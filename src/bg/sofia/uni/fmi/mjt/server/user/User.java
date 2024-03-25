package bg.sofia.uni.fmi.mjt.server.user;

import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmptyMarketException;
import bg.sofia.uni.fmi.mjt.server.exceptions.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import com.google.gson.Gson;

import java.util.Map;

public class User {
    private static final Gson GSON = new Gson();
    private static final double DEFAULT_BALANCE = 0.0;
    private double balance;
    private final String name;
    private final String password;

    public User(String name, String password, double balance) {
        this.name = name;
        this.password = password;
        this.balance = balance;
    }

    public User(String name, String password) {
        this(name, password, DEFAULT_BALANCE);
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof User) && name.equals(((User) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public void acquireAsset(int number, String assetId, Map<String, Currency> assets)
        throws InsufficientBalanceException, EmptyMarketException {

        if (assets.isEmpty()) {
            throw new EmptyMarketException("empty market");
        }

        if (assetId == null || assetId.isEmpty() || assetId.isBlank()) {
            throw new IllegalArgumentException("no asset was found");
        }

        double value = number * assets.get(assetId).priceUsd();

        if (balance < value) {
            throw new InsufficientBalanceException("insufficient balance");
        }

        balance -= value;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public void sellAsset(int number, String assetId, Map<String, Currency> assets) {
        balance += number * assets.get(assetId).priceUsd();
    }

    public void deposit(double amount) throws NegativeValueException {
        if (amount < 0) {
            throw new NegativeValueException("cannot deposit negative value money");
        }

        balance += amount;
    }

    public void withdraw(double amount) throws InsufficientBalanceException, NegativeValueException {
        if (amount > balance) {
            throw new InsufficientBalanceException("the balance insufficient");
        }

        if (amount < 0.0) {
            throw new NegativeValueException("user cannot withdraw negative values");
        }

        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }

    public void buyAsset(String assetId, double amount) {

        balance -= amount;
    }

}
