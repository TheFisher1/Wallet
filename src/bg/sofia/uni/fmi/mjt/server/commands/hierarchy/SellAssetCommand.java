package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.exceptions.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.logging.Level;

public final class SellAssetCommand extends CommandBase {
    private static final int MIN_ARGUMENTS = 2;
    private static final int ASSET_ID = 1;
    private final StockMarket stockMarket;
    public SellAssetCommand(SessionStorage sessionStorage,
                            PersonalWalletStorage personalWalletStorage,
                            LoggingHandler loggingHandler,
                            StockMarket stockMarket, String... line) {
        super(sessionStorage, personalWalletStorage, loggingHandler, line);
        this.stockMarket = stockMarket;
    }

    @Override
    public Optional<String> validate() {
        if (super.args.length < MIN_ARGUMENTS) {
            return Optional.of("Please was no assetId provided");
        }

        if (stockMarket.getCurrencies() == null) {
            return Optional.of("There are currently no assets on the market");
        }

        return Optional.empty();
    }

    @Override
    public String execute(SelectionKey selectionKey) {
        if (validate().isPresent()) {
            return validate().get();
        }

        String username = sessionStorage.get(selectionKey);
        if (username == null) {
            return "user was not logged in";
        }
        try {
            personalWalletStorage.sellAsset(sessionStorage.get(selectionKey),
                                            super.args[ASSET_ID],
                                            stockMarket.getCurrencies());
        } catch (UserNotFoundException e) {
            log(e, Level.FINE, username);
            return "user not found";

        } catch (AssetNotFoundException e) {
            log(e, Level.FINE, username);
            return "there was no asset with " + args[1] + " found";

        } catch (NegativeValueException e) {
            log(e, Level.FINE, username);
            return "cannot sell negative amounts";
        }

        return "successfully sold asset";
    }
}
