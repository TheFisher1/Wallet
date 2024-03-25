package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.exceptions.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Optional;

public final class GetOverallSummaryCommand extends CommandBase {
    private StockMarket stockMarket;
    public GetOverallSummaryCommand(SessionStorage sessionStorage,
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
            return "not logged in";
        }

        if (stockMarket.getCurrencies() == null) {
            return "There are currently no assets on the market";
        }

        try {
            personalWalletStorage.getWalletOverallSummary(sessionStorage.get(selectionKey),
                stockMarket.getCurrencies());

            return "Overall summary: " +
                String.format(FORMAT_VALUE,
                    personalWalletStorage.getWalletOverallSummary(sessionStorage.get(selectionKey),
                        stockMarket.getCurrencies()));
        } catch (UserNotFoundException e) {
            //..
            return "";
        } catch (AssetNotFoundException e) {

            return "";
        }
    }
}
