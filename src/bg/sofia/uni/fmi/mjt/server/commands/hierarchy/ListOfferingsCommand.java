package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Optional;

public final class ListOfferingsCommand extends CommandBase {
    private StockMarket stockMarket;
    public ListOfferingsCommand(SessionStorage sessionStorage,
                                PersonalWalletStorage personalWalletStorage,
                                LoggingHandler loggingHandler, StockMarket stockMarket, String... line) {
        super(sessionStorage, personalWalletStorage, loggingHandler, line);
        this.stockMarket = stockMarket;
    }

    @Override
    public Optional<String> validate() {
        return Optional.empty();
    }

    @Override
    public String execute(SelectionKey selectionKey) {
        if (sessionStorage.get(selectionKey) == null) {
            return "User has not logged in";
        }

        if (stockMarket.getCurrencies() == null) {
            return "There are no assets on the market";
        }

        return System.lineSeparator() + stockMarket.getCurrencies().values().parallelStream()
            .map(currency -> "asset id: " + currency.assetId() +
                " price: " + currency.priceUsd() + System.lineSeparator()).sequential()
            .reduce("", (String s1, String s2) -> s1 + s2, (String s1, String s2) -> s1 + s2);
    }
}
