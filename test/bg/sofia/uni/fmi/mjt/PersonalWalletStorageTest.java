package bg.sofia.uni.fmi.mjt;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.exceptions.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmptyMarketException;
import bg.sofia.uni.fmi.mjt.server.exceptions.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.financials.PersonalWallet;
import bg.sofia.uni.fmi.mjt.server.user.User;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PersonalWalletStorageTest {

//    private static final SecretKey SECRET_KEY = AES.generateSecretKey();
    private static PersonalWalletStorage personalWalletStorage;
    private static final StringWriter stringWriter = new StringWriter();
    private static final double DELTA = 0.0001;
    private static Map<String, User> users = new LinkedHashMap<>();
    private static Map<String, PersonalWallet> personalWalletMap = new LinkedHashMap<>();
    private static Map<String, Currency> assetPrices;
    private static PersonalWalletStorage storage;
    private static final String output = """
        user0;;os95kkIOX/r6pnrlHNNnYQ==;;0.0
        user1;;lutJcbBfdS7niVeQJZ7/OQ==;;0.0
        user2;;Dozi/ASVj4LjOH70XEU7jw==;;0.0
        user3;;9pBPrIDpcPgcUUX7NbDIYw==;;0.0
        user4;;pH5KHHXxQeA/c0qFHORSjA==;;0.0
        """;

    private static final Map<String, Currency> newAssetPrices = Map.of("BTC",
        new Currency("BTC", "bitcoin", 55_000, 1));

    @BeforeAll
    static void setUp() {
        Map<String, List<Currency>> currencyMap = new LinkedHashMap<>();
        currencyMap.put("BTC", List.of(new Currency("BTC", "bitcoin", 40_000, 1)));
        currencyMap.put("SPIKE", List.of(new Currency("SPIKE","SPIKE",200,1)));
        currencyMap.put("YFI3L", List.of(new Currency("YFI3L","YFI3L",0.03205577689931124,1)));

        assetPrices = new LinkedHashMap<>();
        assetPrices.put("BTC", new Currency("BTC", "bitcoin", 40_000, 1));
        assetPrices.put("SPIKE", new Currency("SPIKE","SPIKE",200,1));
        assetPrices.put("YFI3L", new Currency("YFI3L","YFI3L",0.03205577689931124,1));

        PersonalWallet personalWallet = new PersonalWallet(new HashMap<>());
        for (int i = 0; i < 5; ++i) {
            users.put("user" + i, new User("user" + i, "password" + i));
            personalWalletMap.put("user" + i, personalWallet);
        }



        personalWalletStorage = new PersonalWalletStorage(users, personalWalletMap, new LinkedHashMap<>());
        storage = mock(PersonalWalletStorage.class);
    }


    @Test
    void testSaveUsers() throws IOException {
        StringWriter stringWriter = new StringWriter();
        StringWriter stringWriter1 = new StringWriter();
        StringWriter stringWriter2 = new StringWriter();
        PersonalWalletStorage personalWalletStorage1 = new PersonalWalletStorage( users, personalWalletMap, new LinkedHashMap<>());
        personalWalletStorage1.save(stringWriter, stringWriter1, stringWriter2);

        System.out.println(stringWriter1.toString());

    }


    @Test
    void testLoadUsers() throws IOException {
        StringWriter stringWriter1 = new StringWriter();
        PersonalWalletStorage personalWalletStorage1 = new PersonalWalletStorage(users, personalWalletMap, new LinkedHashMap<>());

        StringWriter stringWriter2 = new StringWriter();
        StringWriter stringWriter3 = new StringWriter();

        personalWalletStorage1.save(stringWriter1, stringWriter2, stringWriter3);

        PersonalWalletStorage personalWalletStorage = new PersonalWalletStorage(new LinkedHashMap<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        personalWalletStorage.load(new StringReader(stringWriter1.toString()), new StringReader(stringWriter2.toString()), new StringReader(stringWriter3.toString()));

        Collection<User> users = personalWalletStorage.getUsers();

        assertEquals(personalWalletStorage.getUsers().size(), 5);
        int i = 0;
        for ( ;i < 5; ) {
            assertTrue(users.contains(new User("user" + i, "password" + i++)));
        }
    }

    @Test
    void getUsersUnmodifiableCollection() {
        assertThrows(UnsupportedOperationException.class, () -> personalWalletStorage.getUsers().add(new User(" ", " ")));
    }

    @Test
    void testWithdrawNotEnoughMoney() throws InsufficientBalanceException, UserNotFoundException,
        NegativeValueException {
        String user = "user1";
        assertThrows(InsufficientBalanceException.class, () -> personalWalletStorage.withdraw(user, 500));

        personalWalletStorage.deposit(user, 450);

        assertEquals( 450, personalWalletStorage.getBalance(user), DELTA);

        assertThrows(InsufficientBalanceException.class, () -> personalWalletStorage.withdraw(user, 500));
        assertEquals( 450, personalWalletStorage.getBalance(user), DELTA);
    }

    @Test
    void testBuyAssetNotEnoughMoney() throws InsufficientBalanceException, UserNotFoundException, EmptyMarketException {
        String user = "user9";
        personalWalletStorage.addUser(new User("user9", "password9"));
        assertThrows(InsufficientBalanceException.class, () -> personalWalletStorage.acquireAsset(user, 3, "BTC", assetPrices));

    }

    @Test
    void testBuyAssetAssetNotFound()
        throws UserNotFoundException, InsufficientBalanceException, EmptyMarketException, NegativeValueException {
        String user = "user4";
        personalWalletStorage.deposit("user4", 5 );
        assertThrows(AssetNotFoundException.class, () -> personalWalletStorage.acquireAsset(user, 3, "NON_EXISTING", assetPrices));
    }

    @Test
    void testBuyAssetTransactionHistoryGetCorrectlyGenerated()
        throws UserNotFoundException, InsufficientBalanceException, EmptyMarketException, AssetNotFoundException, NegativeValueException {
        personalWalletStorage.deposit("user3", 50_000);
        personalWalletStorage.acquireAsset("user3", 1, "BTC", assetPrices);

        assertEquals(1, personalWalletStorage.getTransactionHistory("user3").size());

        personalWalletStorage.deposit("user3", 50_000);
        personalWalletStorage.acquireAsset("user3", 1, "BTC", assetPrices);
        assertEquals(2, personalWalletStorage.getTransactionHistory("user3").size());

        assertEquals(0, personalWalletStorage.getTransactionHistory("user2").size());

        personalWalletStorage.sellAsset("user3", "BTC", assetPrices);
        assertEquals(3, personalWalletStorage.getTransactionHistory("user3").size());
    }

    @Test
    void testBuyAssetPersonalAssetsGetCorrectlyOrdered()
        throws UserNotFoundException, InsufficientBalanceException, EmptyMarketException, AssetNotFoundException, NegativeValueException {
        personalWalletStorage.deposit("user1", 50_000);
        personalWalletStorage.acquireAsset("user1", 1, "BTC", assetPrices);

        assertEquals(1, personalWalletStorage.getPersonalAssets("user1").size());

        personalWalletStorage.deposit("user1", 50_000);
        personalWalletStorage.acquireAsset("user1", 1, "SPIKE", assetPrices);
        assertEquals(2, personalWalletStorage.getPersonalAssets("user1").size());

        personalWalletStorage.sellAsset("user1", "BTC", assetPrices);
        assertEquals(1, personalWalletStorage.getPersonalAssets("user1").size());

        personalWalletStorage.acquireAsset("user1", 2, "YFI3L", assetPrices);
        personalWalletStorage.sellAsset("user1", "YFI3L", assetPrices);
    }

    @Test
    void testBalanceGetsUpdatedCorrectly() throws UserNotFoundException, InsufficientBalanceException,
        EmptyMarketException, AssetNotFoundException,  NegativeValueException {
        personalWalletStorage.add(new User("user6", "password6"), new PersonalWallet());
        personalWalletStorage.deposit("user6", 100_000);



        personalWalletStorage.acquireAsset("user6", 40000, "BTC", assetPrices);
        assertEquals(60_000, personalWalletStorage.getBalance("user6"));

        personalWalletStorage.sellAsset("user6", "BTC", newAssetPrices);
        assertEquals(115_000, personalWalletStorage.getBalance("user6"));
    }

    @Test
    void testStocksVolumeGetsRoundedCorrectly()
        throws UserNotFoundException, InsufficientBalanceException, EmptyMarketException, AssetNotFoundException, NegativeValueException {
        personalWalletStorage.addUser(new User("user5", "password5"));

        personalWalletStorage.deposit("user5", 5000);
        personalWalletStorage.acquireAsset("user5", 300, "BTC", assetPrices);
        assertEquals(0.008, personalWalletStorage.getPersonalAssetsFormatted("user5").get("BTC"));

    }

    @Test
    void testGetPersonalAssetsGetsRoundedCorrectly()
        throws UserNotFoundException, InsufficientBalanceException, EmptyMarketException, AssetNotFoundException, NegativeValueException {
        personalWalletStorage.addUser(new User("user7", "password7"));
        personalWalletStorage.deposit("user7", 1000);
        personalWalletStorage.acquireAsset("user7", 1000, "SPIKE", assetPrices);

        personalWalletStorage.getPersonalAssetsFormatted("user7");
        assertEquals(5, personalWalletStorage.getPersonalAssetsFormatted("user7").get("SPIKE"), DELTA);
    }

    @Test
    void testGetSummary()
        throws UserNotFoundException, InsufficientBalanceException, EmptyMarketException, AssetNotFoundException, NegativeValueException {
        personalWalletStorage.addUser(new User("user7", "password7"));
        personalWalletStorage.deposit("user7", 5000);

        personalWalletStorage.acquireAsset("user7", 450, "BTC", assetPrices);

        assertEquals(450.0, personalWalletStorage.getSummary("user7", assetPrices), DELTA);

        personalWalletStorage.sellAsset("user7","BTC", assetPrices);

        assertNotEquals(450.0, personalWalletStorage.getSummary("user7", assetPrices), DELTA);
    }

    @Test
    void testGetOverallSummary()
        throws UserNotFoundException, InsufficientBalanceException, EmptyMarketException, AssetNotFoundException, NegativeValueException {
        personalWalletStorage.addUser(new User("user8", "password8"));
        personalWalletStorage.deposit("user8", 5000);

        personalWalletStorage.acquireAsset("user8",2500, "BTC", assetPrices);

        assertEquals(0.0, personalWalletStorage.getWalletOverallSummary("user8", assetPrices));

        personalWalletStorage.sellAsset("user8", "BTC", newAssetPrices);

        assertEquals(personalWalletStorage.getWalletOverallSummary("user8", assetPrices), 937.5, DELTA);
    }

    @Test
    void testGetOverallSummaryNoTransactions() throws UserNotFoundException, AssetNotFoundException {
        personalWalletStorage.addUser(new User("user9", "password9"));
       assertDoesNotThrow(() -> personalWalletStorage.getWalletOverallSummary("user9", assetPrices));
       assertEquals(0.0, personalWalletStorage.getWalletOverallSummary("user9", assetPrices));
    }

    @Test
    void testBuyAssetsInsufficientBalanceDoesNotAddAssets() {
        personalWalletStorage.addUser(new User("user10", "password10"));
        assertEquals(0, personalWalletStorage.getPersonalAssets("user10").size());

        assertThrows(InsufficientBalanceException.class, () -> personalWalletStorage.acquireAsset("user10", 50.0, "BTC",assetPrices));
        assertTrue(personalWalletStorage.getPersonalAssets("user10").isEmpty());
    }

    @Test
    void testBuyAssetThrowsUserNotFoundException() {
        assertThrows(UserNotFoundException.class, () -> personalWalletStorage.acquireAsset("non-existing-user", 2, "BTC", assetPrices));
    }

}
