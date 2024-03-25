package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.exceptions.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.logging.Level;

public final class GetSummaryCommand extends CommandBase {
    private final StockMarket stockMarket;
    public GetSummaryCommand(SessionStorage sessionStorage,
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
        String user = sessionStorage.get(selectionKey);
        if (user == null) {
            return "user has not logged in";
        }

        if (stockMarket.getCurrencies() == null) {
            return "There are currently no available currencies so " +
                "it is not possible to evaluate the currently owned currencies.";
        }

        try {
            double balance = personalWalletStorage.getSummary(sessionStorage.get(selectionKey),
                                    stockMarket.getCurrencies());
            return "summary: " + String.format(FORMAT_VALUE, balance);
        } catch (UserNotFoundException e) {
            log(e, Level.FINE, user);
            return "user could not be found";
        } catch (AssetNotFoundException e) {
            log(e, Level.FINE, user);
            return "there has been a change in market currencies and some of previously bought are not offered";
        }

    }
}
