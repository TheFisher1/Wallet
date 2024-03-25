package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.logging.Level;

public final class LogOutCommand extends CommandBase {
    public LogOutCommand(SessionStorage sessionStorage,
                         PersonalWalletStorage personalWalletStorage, LoggingHandler loggingHandler, String... line) {
        super(sessionStorage, personalWalletStorage, loggingHandler, line);
    }

    @Override
    public Optional<String> validate() {
        return Optional.empty();
    }

    @Override
    public String execute(SelectionKey selectionKey) {
        String user = sessionStorage.get(selectionKey);
        if (user != null) {
            sessionStorage.remove(selectionKey);
        } else {
            return "user has already logged out";
        }

        try {
            personalWalletStorage.save();
        } catch (IOException e) {
            log(e, Level.WARNING, user);
        }

        return "successful logout";
    }
}
