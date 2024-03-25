package bg.sofia.uni.fmi.mjt.server.commands;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.access.StockMarketConfigurator;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorageSaver;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;

import java.util.LinkedHashMap;

public class CommandExecutor {
    private final PersonalWalletStorage personalWalletStorage;
    private final StockMarket stockMarket;
    private final SessionStorage userSessionStorage;
    private final LoggingHandler log;

    private PersonalWalletStorageSaver personalWalletStorageSaver;

    public CommandExecutor(LoggingHandler loggingHandler) {
        this.stockMarket = new StockMarket(StockMarketConfigurator.builder(null).build());
        this.personalWalletStorage = new PersonalWalletStorage();
        this.personalWalletStorageSaver = new PersonalWalletStorageSaver(personalWalletStorage);
        this.userSessionStorage = new SessionStorage(new LinkedHashMap<>());
        this.log = loggingHandler;

        this.personalWalletStorageSaver.schedule();
        this.stockMarket.schedule();
    }

    public CommandExecutor(StockMarket stockMarket,
                           PersonalWalletStorage personalWalletStorage,
                           SessionStorage sessionStorage,
                           LoggingHandler loggingHandler) {

        this.stockMarket = stockMarket;
        this.personalWalletStorage = personalWalletStorage;
        this.personalWalletStorageSaver = new PersonalWalletStorageSaver(personalWalletStorage);
        this.userSessionStorage = sessionStorage;
        this.log = loggingHandler;
    }

    public String execute(SelectionKey selectionKey, String line) {
        return CommandGenerator
                .getCommand(userSessionStorage, personalWalletStorage, log, line, stockMarket)
                .execute(selectionKey);
    }

    public void shutdown() {
        personalWalletStorageSaver.shutdown();
        stockMarket.shutdown();
    }

}


