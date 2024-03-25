package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.financials.PersonalWallet;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;
import bg.sofia.uni.fmi.mjt.server.user.User;

import java.nio.channels.SelectionKey;
import java.util.Optional;

public final class RegisterCommand extends CommandBase {
    private static final int USERNAME = 1;
    private static final int PASSWORD = 2;

    private static final int MINIMAL_ARGS_LENGTH = 3;
    public RegisterCommand(SessionStorage sessionStorage,
                           PersonalWalletStorage personalWalletStorage, LoggingHandler loggingHandler, String... line) {
        super(sessionStorage, personalWalletStorage, loggingHandler, line);

    }

    @Override
    public Optional<String> validate() {

        if (super.args.length < MINIMAL_ARGS_LENGTH) {
            return Optional.of("Please enter both username and password");
        } else if (personalWalletStorage.getUser(super.args[USERNAME]).isPresent()) {
            return Optional.of("already registered user");
        }

        return Optional.empty();
    }

    @Override
    public String execute(SelectionKey selectionKey) {
        if (validate().isPresent()) {
            return validate().get();
        }

        if (sessionStorage.get(selectionKey) != null) {
            return "Please first log-out and then you can register new users";
        }

        String username = super.args[USERNAME];
        String password = super.args[PASSWORD];

        personalWalletStorage.add(new User(username, password), new PersonalWallet());
        return "successfully added user with name: " + username;

    }
}

