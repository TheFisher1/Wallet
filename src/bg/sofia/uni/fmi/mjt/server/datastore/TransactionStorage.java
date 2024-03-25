package bg.sofia.uni.fmi.mjt.server.datastore;

import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.financials.Transaction;
import bg.sofia.uni.fmi.mjt.server.utils.TextProcessor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class TransactionStorage {

    private final Map<String, Map<String, Transaction>> transactions = new LinkedHashMap<>();
    private TextProcessor textProcessor;
    public TransactionStorage(Map<String, Map<String, Transaction>> transactions) {
        this.transactions.putAll(transactions);
        this.textProcessor = new TextProcessor();
    }

    public TransactionStorage(String filename) {
        textProcessor = new TextProcessor();

        Map<String, Map<String, Transaction>> map = new LinkedHashMap<>();
        try (Reader fileReader = new FileReader(filename)) {
            map = textProcessor.loadTransactionHistory(fileReader);
        } catch (IOException e) {
            //.. ignored
        }

        if (map != null) {
            transactions.putAll(map);
        }

    }

    public TransactionStorage(Reader reader) {
        textProcessor = new TextProcessor();

        Map<String, Map<String, Transaction>> map = new LinkedHashMap<>();
        try {
            map = textProcessor.loadTransactionHistory(reader);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        if (map != null) {
            transactions.putAll(map);
        }
    }

    public Map<String, Map<String, Transaction>> getTransactionHistory() {
        return Collections.unmodifiableMap(this.transactions);
    }

    public void acquireAsset(String user, double amount, String assetId, Map<String, Currency> assetPrices) {
        transactions.putIfAbsent(user, new LinkedHashMap<>());

        var map = transactions.get(user);
        map.put(LocalDateTime.now().toString(), new Transaction(Transaction.Status.BUY,
                                                user,
                                                amount / assetPrices.get(assetId).priceUsd(),
                                                assetPrices.get(assetId),
                                                LocalDateTime.now().toString()));

        this.transactions.put(user, map);
    }

    public void sellAsset(String user, double number, String assetId, Map<String, Currency> assetPrices) {
        var map = transactions.get(user);

        map.put(LocalDateTime.now().toString(),
            new Transaction(
                Transaction.Status.SELL,
                user,
                number,
                assetPrices.get(assetId),
                LocalDateTime.now().toString()));

        transactions.put(user, map);
    }

    public void save(Writer writer) throws IOException {
        if (textProcessor != null) {
            textProcessor.saveTransactionHistory(writer, getTransactionHistory());
        }
    }

    public Map<String, Transaction> getTransactions(String username) {
        return this.transactions.get(username);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransactionStorage)) {
            return false;
        }

        return this.transactions.equals(((TransactionStorage) o).transactions);
    }

    @Override
    public int hashCode() {
        return transactions.hashCode();
    }

}
