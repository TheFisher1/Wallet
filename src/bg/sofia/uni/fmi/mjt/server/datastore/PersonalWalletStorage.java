package bg.sofia.uni.fmi.mjt.server.datastore;

import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.exceptions.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmptyMarketException;
import bg.sofia.uni.fmi.mjt.server.exceptions.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.financials.PersonalWallet;
import bg.sofia.uni.fmi.mjt.server.financials.Transaction;
import bg.sofia.uni.fmi.mjt.server.user.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PersonalWalletStorage implements Runnable {

    private static final String USERS_FILENAME = "users.txt";
    private static final String ASSETS_STORAGE_FILENAME = "assetStorage.txt";
    private static final String TRANSACTION_FILENAME = "transactions.txt";
    private static final String VALUE_FORMAT = "%.3f";
    private UserStorage userStorage;
    private PersonalAssetStorage personalAssetStorage;
    private TransactionStorage transactionHistory;
    private String usersFile;

    public PersonalWalletStorage(String usersFile, String assetsStorageFilename, String transactionFilename) {
        this.usersFile = usersFile;
        this.userStorage = new UserStorage(usersFile);
        this.personalAssetStorage = new PersonalAssetStorage(userStorage.getKeys(), assetsStorageFilename);
        this.transactionHistory = new TransactionStorage(transactionFilename);
    }

    public PersonalWalletStorage(Map<String, User> users,
                                 Map<String, PersonalWallet> assetStorageMap,
                                 Map<String, Map<String, Transaction>> transactionHistory) {

        this.userStorage = new UserStorage(users);
        this.personalAssetStorage = new PersonalAssetStorage(assetStorageMap);
        this.transactionHistory = new TransactionStorage(transactionHistory);
        this.usersFile = USERS_FILENAME;
    }

    public PersonalWalletStorage() {
        this(USERS_FILENAME, ASSETS_STORAGE_FILENAME, TRANSACTION_FILENAME);
    }

    public void save(Writer usersWriter,
                     Writer personalAssetsWriter,
                     Writer transctionHistoryWriter) throws IOException {

        userStorage.save(usersWriter);
        personalAssetStorage.save(personalAssetsWriter);
        transactionHistory.save(transctionHistoryWriter);
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(userStorage.getValues());
    }

    public void load(Reader readerUsers, Reader readerAssets, Reader readerTransactions) {
        userStorage = new UserStorage(userStorage.load(readerUsers));
        personalAssetStorage = new PersonalAssetStorage(personalAssetStorage.load(readerAssets));
        transactionHistory = new TransactionStorage(readerTransactions);
    }

    public void save() throws IOException {
        userStorage.save();
        personalAssetStorage.saveAll(new FileWriter("assetStorage.txt"));
        transactionHistory.save(new FileWriter("transactions.txt"));
    }

    public void addUser(User user) {
        userStorage.add(user.getName(), user);
        personalAssetStorage.add(user.getName(), new PersonalWallet());
    }

    public double getBalance(String username) {
        return userStorage.get(username).getBalance();
    }

    public void add(User key, PersonalWallet value) {
        userStorage.add(key.getName(), key);
        personalAssetStorage.add(key.getName(), value);
    }

    public PersonalWallet get(User key) {
        return personalAssetStorage.get(key.getName());
    }

    @Override
    public void run() {
        try {
            save();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public Map<String, Double> getPersonalAssets(String user) {
        return personalAssetStorage.get(user).get();
    }

    public Map<String, Double> getPersonalAssetsFormatted(String user) {
        Map<String, Double> assets = personalAssetStorage.get(user).get();
        Map<String, Double> ans = new LinkedHashMap<>();
        for (var entry : assets.entrySet()) {
            ans.put(entry.getKey(), Double.parseDouble(String.format(VALUE_FORMAT, entry.getValue())));
        }

        return Collections.unmodifiableMap(ans);
    }

    public void deposit(String user, double amount) throws UserNotFoundException, NegativeValueException {
        userStorage.deposit(user, amount);
    }

    public void withdraw(String user, double amount)
        throws InsufficientBalanceException, UserNotFoundException, NegativeValueException {
        userStorage.withdraw(user, amount);
    }

    public void acquireAsset(String user, double amount, String assetId, Map<String, Currency> assetPrices)
        throws InsufficientBalanceException, EmptyMarketException,
        UserNotFoundException, AssetNotFoundException, NegativeValueException {

        if (userStorage.get(user) == null) {
            throw new UserNotFoundException("user with username " + user + " could not be found");
        } else  if (userStorage.get(user).getBalance() < amount) {
            throw new InsufficientBalanceException("the balance of the user is insufficient");
        }

        personalAssetStorage.buyAsset(user, amount, assetId, assetPrices);
        userStorage.withdraw(user, amount);
        transactionHistory.acquireAsset(user, amount, assetId, assetPrices);

    }

    public void sellAsset(String user, String assetId, Map<String, Currency> assetPrices)
        throws UserNotFoundException, AssetNotFoundException, NegativeValueException {

        if (personalAssetStorage.get(user).get(assetId) == null) {
            throw new AssetNotFoundException("Asset with assetId: " + assetId + " is not currently owned");
        }

        double amount = personalAssetStorage.get(user).get(assetId);
        personalAssetStorage.sellAsset(user, assetId, assetPrices);
        userStorage.deposit(user, amount * assetPrices.get(assetId).priceUsd() );
        transactionHistory.sellAsset(user, amount, assetId, assetPrices);

    }

    public Optional<User> getUser(String username) {
        if (userStorage.get(username) != null) {
            return Optional.of(userStorage.get(username));
        }

        return Optional.empty();
    }

    public Map<String, Transaction> getTransactionHistory(String username) {
        if (transactionHistory.getTransactions(username) != null) {
            return Collections.unmodifiableMap(transactionHistory.getTransactions(username));
        }

        return new LinkedHashMap<>();
    }

    public double getSummary(String username, Map<String, Currency> assetPrices)
        throws UserNotFoundException, AssetNotFoundException {
        PersonalWallet personalWallet = personalAssetStorage.get(username);

        if (personalWallet == null) {
            throw new UserNotFoundException("user with name: " + username + " could not be found");
        }

        if (personalWallet.get().keySet().stream().anyMatch(key -> assetPrices.get(key) == null)) {
            throw new AssetNotFoundException("not all assets previously bought are now on the market");
        }

        return personalWallet.get()
            .keySet()
            .stream()
            .mapToDouble(assetId -> personalWallet.get(assetId) * assetPrices.get(assetId).priceUsd())
            .sum();
    }

    public double getWalletOverallSummary(String username, Map<String, Currency> assetPrices)
        throws UserNotFoundException, AssetNotFoundException {
        Map<String, Transaction> transactions = transactionHistory.getTransactions(username);

        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }

        Map<Transaction.Status, List<Transaction>> transactionsGrouped = group(transactions);
        List<Transaction> buyTransactions = transactionsGrouped.get(Transaction.Status.BUY);
        List<Transaction> sellTransactions = transactionsGrouped.get(Transaction.Status.SELL);

        double exprenses = 0.0;
        double income = 0.0;

        if (buyTransactions != null) {
            exprenses = sumCosts(buyTransactions);
        }

        if (sellTransactions != null) {
            income = sumCosts(sellTransactions);
        }

        return income - exprenses + getSummary(username, assetPrices);
    }

    private Map<Transaction.Status, List<Transaction>> group(Map<String, Transaction> transactions) {
        return transactions.values()
            .stream()
            .collect(Collectors.groupingBy(
                Transaction::getTransactionStatus)
            );
    }

    private double sumCosts(List<Transaction> transactions) {
        return transactions.stream()
                .mapToDouble((Transaction transaction) -> transaction.getAmount() * transaction.getAsset().priceUsd())
                .sum();
    }
}
