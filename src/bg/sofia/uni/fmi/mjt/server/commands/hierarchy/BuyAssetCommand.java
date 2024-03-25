package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.exceptions.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmptyMarketException;
import bg.sofia.uni.fmi.mjt.server.exceptions.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.logging.Level;

public final class BuyAssetCommand extends CommandBase {
    private final StockMarket stockMarket;
    private static final int MINIMAL_LENGTH = 3;
    private static final int AMOUNT = 1;
    private static final int ASSET_ID = 2;
    public BuyAssetCommand(SessionStorage sessionStorage,
                           PersonalWalletStorage personalWalletStorage,
                           LoggingHandler loggingHandler,
                           StockMarket stockMarket,
                           String... line) {

        super(sessionStorage, personalWalletStorage, loggingHandler, line);
        this.stockMarket = stockMarket;
    }

    @Override
    public Optional<String> validate() {
        if (args.length < MINIMAL_LENGTH) {
            return Optional.of("please provide both amount and assetId");
        }

        return Optional.empty();
    }

    @Override
    public String execute(SelectionKey selectionKey) {
        if (validate().isPresent()) {
            return validate().get();
        }

        String user = sessionStorage.get(selectionKey);

        if (user == null) {
            return "User has not logged in";
        }

        if (stockMarket.getCurrencies() == null) {
            return "There are currently no available resources";
        }

        try {
            Double.parseDouble(args[AMOUNT]);
        } catch (NumberFormatException e) {
            log(e, Level.FINE, user);
            return "Please enter correct amount of money";
        }

        return buy(user);
    }

    private String buy(String username) {
        try {
            personalWalletStorage.acquireAsset(username, Double.parseDouble(args[AMOUNT]),
                args[ASSET_ID], stockMarket.getCurrencies());

        } catch (InsufficientBalanceException e) {
            log(e, Level.FINE, username);
            return "insufficient balance";
        } catch (EmptyMarketException e) {
            log(e, Level.FINE, username);
            return "there were no currencies";
        } catch (UserNotFoundException e) {
            log(e, Level.FINE, username);
            return "there has been error logging in";
        } catch (AssetNotFoundException e) {
            log(e, Level.FINE, username);
            return "asset with id:" + args[ASSET_ID] + " is not currently available";
        } catch (NegativeValueException e) {
            log(e, Level.FINE, username);
            return "cannot buy asset with id: " + args[ASSET_ID] + " because value comes to be negative";
        }

        return "successful buy";
    }
}
