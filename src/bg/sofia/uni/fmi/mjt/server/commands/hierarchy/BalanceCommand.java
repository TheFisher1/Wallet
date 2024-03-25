package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Optional;

public final class BalanceCommand extends CommandBase {
    private static final String VALUE_FORMAT = "%.3f";
    public BalanceCommand(SessionStorage sessionStorage,
                          PersonalWalletStorage personalWalletStorage,
                          LoggingHandler loggingHandler,
                          String... line) {

        super(sessionStorage, personalWalletStorage, loggingHandler, line);

    }

    @Override
    public Optional<String> validate() {
        return Optional.empty();
    }

    @Override
    public String execute(SelectionKey selectionKey) {
        if (sessionStorage.get(selectionKey) == null) {
            return "user has not logged in";
        }

        String username = sessionStorage.get(selectionKey);
        return "balance: " + String.format(VALUE_FORMAT, personalWalletStorage.getBalance(username));
    }
}
