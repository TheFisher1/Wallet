package bg.sofia.uni.fmi.mjt;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.commands.CommandExecutor;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.GetSummaryCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.ListAssetsCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.ListOfferingsCommand;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.exceptions.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.CurrencyRetrievalException;
import bg.sofia.uni.fmi.mjt.server.exceptions.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;
import bg.sofia.uni.fmi.mjt.server.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.ls.LSException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.channels.SelectionKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandExecutorTest {
    private static SessionStorage sessionStorage;
    private static PersonalWalletStorage personalWalletStorage;
    private static LoggingHandler loggingHandler;
    private static StockMarket stockMarket;
    private static CommandExecutor commandExecutor;
    private static HttpClient httpClient;

    private static final String MESSAGE_NOT_REGISTERED = "No such user. Please sign up";


    @BeforeEach
    void setUp() {
        sessionStorage = mock(SessionStorage.class);
        personalWalletStorage = mock(PersonalWalletStorage.class);
        loggingHandler = mock(LoggingHandler.class);
        stockMarket = mock(StockMarket.class);

        commandExecutor = new CommandExecutor(stockMarket, personalWalletStorage, sessionStorage, loggingHandler);
    }

    @Test
    void testLogInNotRegisteredUser() {
        verify(sessionStorage, times(0)).add(null, null);
        assertEquals(MESSAGE_NOT_REGISTERED, commandExecutor.execute(null, "log-in user password"));
    }

    @Test
    void testLogInRegisteredUserNoProvidedPassword() {
        assertEquals( "Please enter both username and password",commandExecutor.execute(null, "log-in user5  "));

    }

    @Test
    void testBalanceCommand() {
        when(sessionStorage.get(any())).thenReturn("user5");
        commandExecutor.execute(any(), "balance");
        verify(personalWalletStorage).getBalance("user5");
    }

    @Test
    void LogInCommand() throws IOException {
        String command = "log-in user password";
        when(sessionStorage.get(any())).thenReturn(null);
        when(personalWalletStorage.getUser(any())).thenReturn(Optional.of(new User("user", "password")));
        assertEquals("successful login", commandExecutor.execute(any(), command));
        verify(sessionStorage).add(any(), any());
    }

    @Test
    void testRegisterNoProvidedPassword() {
        String command = "register";
        assertDoesNotThrow(() -> commandExecutor.execute(null, command));
        verify(personalWalletStorage, times(0)).addUser(any());
    }

    @Test
    void testStockMarketThrows() {
        SelectionKey selectionKey = mock(SelectionKey.class);
        User user = mock(User.class);
        when(stockMarket.getCurrencies()).thenThrow(CurrencyRetrievalException.class);
        when(sessionStorage.get(any())).thenReturn("user5");
        when(personalWalletStorage.getUser("user5")).thenReturn(Optional.of(user));
        when(user.getBalance()).thenReturn(600.0);

        String command = "buy-asset 500 BTC";

        assertThrows(CurrencyRetrievalException.class, () -> commandExecutor.execute(selectionKey, "buy-asset 500 BTC"));
    }

    @Test
    void testListOfferings() {
        when(stockMarket.getCurrencies()).thenReturn(null);
        when(sessionStorage.get(any())).thenReturn("user5");
        assertDoesNotThrow(() -> commandExecutor.execute(null, "list-offerings"));
    }

    @Test
    void testWithdrawCommandNegativeSum()
        throws UserNotFoundException, NegativeValueException, InsufficientBalanceException {
        assertDoesNotThrow(() -> personalWalletStorage.withdraw("user", -2.5));

        personalWalletStorage.withdraw("user", -1);
        assertEquals(0, personalWalletStorage.getBalance("user"));
    }

    @Test
    void testListOfferingsDoesNotThrowWhenCurrenciesNull() {
        when(sessionStorage.get(any())).thenReturn("string");
        when(stockMarket.getCurrencies()).thenReturn(null);
        ListOfferingsCommand listAssetsCommand = new ListOfferingsCommand(sessionStorage, personalWalletStorage, loggingHandler, stockMarket, "list-offerings");

        assertDoesNotThrow(() -> listAssetsCommand.execute(null));
        assertEquals("There are no assets on the market", listAssetsCommand.execute(null));
    }

    @Test
    void testGetSummaryCommand() throws UserNotFoundException, AssetNotFoundException {
        when(sessionStorage.get(any())).thenReturn("User");

        GetSummaryCommand getSummaryCommand = new GetSummaryCommand(sessionStorage, personalWalletStorage, loggingHandler, stockMarket, "get-summary");
        getSummaryCommand.execute(null);
        assertEquals("summary: 0.000", getSummaryCommand.execute(null));
    }

}
