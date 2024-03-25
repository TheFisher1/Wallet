package bg.sofia.uni.fmi.mjt;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.commands.CommandExecutor;
import bg.sofia.uni.fmi.mjt.server.commands.CommandGenerator;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.BuyAssetCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.CommandBase;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.DepositCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.EmptyCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.GetOverallSummaryCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.ListOfferingsCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.LogOutCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.SellAssetCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.UnknownCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.WithdrawCommand;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.exceptions.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandCreatorTest {
    private static SessionStorage sessionStorage;
    private static PersonalWalletStorage personalWalletStorage;
    private static LoggingHandler loggingHandler;
    private static StockMarket stockMarket;
    private static CommandExecutor commandExecutor;

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
    void testCreateCommandWithZeroArguments() {
        String commandLine = "log-out";

        CommandBase commandBase =
            CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, commandLine,
                stockMarket);

        assertTrue(commandBase instanceof LogOutCommand);
        assertTrue(commandBase.validate().isEmpty());
    }

    @Test
    void testCreateCommandSellAssetCommand()
        throws UserNotFoundException, AssetNotFoundException, NegativeValueException {
        String commandLine = "sell-asset BTC";
        when(sessionStorage.get(any())).thenReturn("user5");
        CommandBase command =
            CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, commandLine,
                stockMarket);

        assertTrue(command instanceof SellAssetCommand);
        assertEquals("successfully sold asset", command.execute(null));
        verify(personalWalletStorage).sellAsset(any(), any(), any());
    }

    @Test
    void testCreateCommandWith1Argument() {
        String commandLine = "deposit 500";
        CommandBase command =
            CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, commandLine,
                stockMarket);

        assertTrue(command instanceof DepositCommand);
    }

    @Test
    void testCreateCommandMoreThanOneSpaceInterval() {
        String commandLine = "      buy-asset      2      BTC   ";
        CommandBase command =
            CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, commandLine,
                stockMarket);
        assertTrue(command instanceof BuyAssetCommand);
        assertDoesNotThrow(
            () -> CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, commandLine,
                stockMarket));
    }

    @Test
    void testCreateCommandEmptyCommand() {
        String commandLine = "";
        CommandBase command =
            CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, commandLine,
                stockMarket);
        assertTrue(command instanceof EmptyCommand);
        assertDoesNotThrow(
            () -> CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, commandLine,
                stockMarket));
    }

    @Test
    void testInvalidNumberFormat() {
        String commandLine = " deposit _";
        CommandBase command =
            CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, commandLine,
                stockMarket);

        assertDoesNotThrow(() -> command.execute(null));
        assertEquals("Invalid amount entered", command.execute(null));
    }


    @Test
    void testLogOutCommand() throws IOException {
        String command = "log-out";
        CommandBase commandBase =
            CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, command, stockMarket);
        when(sessionStorage.get(any())).thenReturn("user5");
        commandBase.execute(any());

        verify(personalWalletStorage).save();
    }

    @Test
    void testWithdrawCommand() {
        String command = "withdraw 500";
        CommandBase commandBase =
            CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, command, stockMarket);
        assertTrue(commandBase instanceof WithdrawCommand);
    }

    @Test
    void testWithdrawNoArguments() {
        String command = "withdraw";
        CommandBase commandBase = CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, command, stockMarket);
        assertTrue(commandBase instanceof WithdrawCommand);
        assertDoesNotThrow(() -> commandBase.execute(null));
    }

    @Test
    void testBuyAssetNoArguments() {
        String command = "buy-asset";
        CommandBase commandBase = CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, command, stockMarket);
        assertTrue(commandBase instanceof BuyAssetCommand);
        assertDoesNotThrow(() -> commandBase.execute(null));
    }

    @Test
    void testBuyAssetOneArgument() {
        String command = "buy-asset BTC";
        CommandBase commandBase = CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, command, stockMarket);
        assertTrue(commandBase instanceof BuyAssetCommand);
        assertDoesNotThrow(() -> commandBase.execute(null));
    }

    @Test
    void testEmptyCommand() {
        String command = "";
        CommandBase commandBase = CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, command, stockMarket);
        assertTrue(commandBase instanceof EmptyCommand);
    }

    @Test
    void testBlankCommand() {
        String command = "        ";
        CommandBase commandBase = CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, command, stockMarket);
        assertTrue(commandBase instanceof UnknownCommand);
    }

    @Test
    void testGetOverallSummaryCommand() {
        String command = "get-overall-summary";
        CommandBase commandBase = CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, command, stockMarket);
        assertTrue(commandBase instanceof GetOverallSummaryCommand);
    }

    @Test
    void testListOfferingsCommand() {
        String command = "list-offerings";
        CommandBase commandBase = CommandGenerator.getCommand(sessionStorage, personalWalletStorage, loggingHandler, command, stockMarket);
        assertTrue(commandBase instanceof ListOfferingsCommand);
    }
}
