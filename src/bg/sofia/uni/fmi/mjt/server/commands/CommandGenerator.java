package bg.sofia.uni.fmi.mjt.server.commands;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.BalanceCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.BuyAssetCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.CommandBase;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.DepositCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.EmptyCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.GetOverallSummaryCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.GetSummaryCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.ListAssetsCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.ListOfferingsCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.LogInCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.LogOutCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.RegisterCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.SellAssetCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.UnknownCommand;
import bg.sofia.uni.fmi.mjt.server.commands.hierarchy.WithdrawCommand;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

public class CommandGenerator {

    private static final String REGEX_PATTERN = "\\s+";

    public static CommandBase getCommand(SessionStorage sessionStorage,
                                         PersonalWalletStorage personalWalletStorage,
                                         LoggingHandler loggingHandler,
                                         String line,
                                         StockMarket stockMarket) {
        if (line == null || line.isEmpty()) {
            return new EmptyCommand(sessionStorage, personalWalletStorage, loggingHandler, line);
        }

        String[] args = line.replaceAll(REGEX_PATTERN, " ").strip().split(" ");

        return switch (args[0]) {
            case "deposit" -> new DepositCommand(sessionStorage,
                                personalWalletStorage, loggingHandler, args);
            case "list-assets" -> new ListAssetsCommand(sessionStorage,
                                    personalWalletStorage, loggingHandler, args);
            case "log-in" -> new LogInCommand(sessionStorage,
                                    personalWalletStorage, loggingHandler, args);
            case "log-out" -> new LogOutCommand(sessionStorage,
                                personalWalletStorage, loggingHandler, args);
            case "register" -> new RegisterCommand(sessionStorage,
                                personalWalletStorage, loggingHandler, args);
            case "sell-asset" -> new SellAssetCommand(sessionStorage,
                                    personalWalletStorage, loggingHandler, stockMarket, args);
            case "withdraw" -> new WithdrawCommand(sessionStorage,
                                personalWalletStorage, loggingHandler, args);
            case "buy-asset" -> new BuyAssetCommand(sessionStorage,
                                    personalWalletStorage, loggingHandler, stockMarket, args);
            case "balance" -> new BalanceCommand(sessionStorage,
                                personalWalletStorage, loggingHandler, args);
            case "list-offerings" -> new ListOfferingsCommand(sessionStorage, personalWalletStorage,
                                            loggingHandler, stockMarket, args);
            case "get-summary" -> new GetSummaryCommand(sessionStorage, personalWalletStorage,
                                                        loggingHandler, stockMarket, args);
            case "get-overall-summary" -> new GetOverallSummaryCommand(sessionStorage, personalWalletStorage,
                                                            loggingHandler, stockMarket, args);
            default -> new UnknownCommand(sessionStorage,
                            personalWalletStorage, loggingHandler, args);
        };
    }
}
