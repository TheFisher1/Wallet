package bg.sofia.uni.fmi.mjt;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalAssetStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.TransactionStorage;
import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.financials.PersonalWallet;
import bg.sofia.uni.fmi.mjt.server.financials.Transaction;
import bg.sofia.uni.fmi.mjt.server.user.User;
import bg.sofia.uni.fmi.mjt.server.utils.TextProcessor;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TextProcessorTest {

    private static final Gson GSON = new Gson();
    private static String transactionHistoryJSON;
    private static LocalDateTime time;

    @BeforeAll
    static void setUp() {
        Map<String, Map<String, Transaction>> transactionMap = new LinkedHashMap<>();
        time = LocalDateTime.of(2024, 4, 5, 6, 31);

        transactionMap.put("user1", Map.of(time.toString(), new Transaction(Transaction.Status.BUY, "user1", 3.0, new Currency("BTC", "bitcoin", 40_000, 1), time.toString())));
        TransactionStorage transactionStorage = new TransactionStorage(transactionMap);

        transactionHistoryJSON = GSON.toJson(transactionStorage.getTransactionHistory());
    }

    @Test
    void testLoadTransactionHistory() throws IOException {
        TextProcessor textProcessor = new TextProcessor();
        assertEquals(textProcessor.loadTransactionHistory(new StringReader(transactionHistoryJSON)).get("user1").keySet().size(), 1);
    }

    @Test
    void testLoadPersonalAssets() {
        Map<String, PersonalWallet> map = new LinkedHashMap<>();
        map.put("user1", new PersonalWallet(Map.of("BTC", 0.0001)));
        PersonalAssetStorage personalAssetStorage = new PersonalAssetStorage(map);
        StringWriter stringWriter = new StringWriter();

        TextProcessor textProcessor = new TextProcessor();
        textProcessor.savePersonalAssets(stringWriter, personalAssetStorage.getAll());

        assertDoesNotThrow(() -> textProcessor.loadPersonalAssets(new StringReader(stringWriter.toString())));

        var ans = textProcessor.loadPersonalAssets(new StringReader(stringWriter.toString()));
        assertTrue(map.keySet().containsAll(ans.keySet()) &&
                            ans.keySet().containsAll(map.keySet()) &&
                            ans.values().containsAll(map.values()) &&
                            map.values().containsAll(ans.values()));

    }

    @Test
    void testLoadPersonalAssetsHandlesIOException() {
        StringReader stringReader = new StringReader("");
        stringReader.close();

        TextProcessor textProcessor = new TextProcessor();
        textProcessor.loadPersonalAssets(stringReader);
    }

    @Test
    void testAddUser() throws IOException {
        TextProcessor textProcessor = new TextProcessor();
        StringWriter stringWriter = new StringWriter();
        textProcessor.addUser(new User("user5", "password5"), stringWriter);
        assertEquals("user5", stringWriter.toString().split(";;")[0]);
    }

}
