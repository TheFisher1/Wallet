package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Optional;

public final class ListAssetsCommand extends CommandBase {
    public ListAssetsCommand(SessionStorage sessionStorage,
                             PersonalWalletStorage personalWalletStorage, LoggingHandler loggingHandler,
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
            return "user not logged in";
        }

        String username = sessionStorage.get(selectionKey);
        return personalWalletStorage.getPersonalAssetsFormatted(username).toString();
    }

}
