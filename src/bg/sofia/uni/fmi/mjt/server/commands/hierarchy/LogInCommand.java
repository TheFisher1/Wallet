package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;
import bg.sofia.uni.fmi.mjt.server.user.User;

import java.nio.channels.SelectionKey;
import java.util.Optional;

public final class LogInCommand extends CommandBase {
    private static final int USERNAME = 1;
    private static final int PASSWORD = 2;
    private static final int MINIMAL_LENGTH = 3;
    public LogInCommand(SessionStorage sessionStorage,
                        PersonalWalletStorage personalWalletStorage, LoggingHandler loggingHandler, String... line) {
        super(sessionStorage, personalWalletStorage, loggingHandler, line);
    }

    @Override
    public Optional<String> validate() {
        if (args.length < MINIMAL_LENGTH) {
            return Optional.of("Please enter both username and password");
        }
        return Optional.empty();
    }

    @Override
    public String execute(SelectionKey selectionKey) {
        if (sessionStorage.get(selectionKey) != null) {
            return "Already logged in";
        }

        if (validate().isPresent()) {
            return validate().get();
        }

        Optional<User> toBeLoggedIn = personalWalletStorage.getUser(args[USERNAME]);

        if (toBeLoggedIn.isEmpty()) {
            return "No such user. Please sign up";
        }

        if (toBeLoggedIn.get().getPassword().equals(args[PASSWORD])) {
            sessionStorage.add(selectionKey, toBeLoggedIn.get().getName());
            return "successful login";
        } else {
            return "wrong password or username";
        }
    }
}
