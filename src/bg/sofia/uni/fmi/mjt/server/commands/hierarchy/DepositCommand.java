package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.logging.Level;

public final class DepositCommand extends CommandBase {
    private static final int AMOUNT = 1;
    private static final int MIN_LENGTH = 2;
    public DepositCommand(SessionStorage sessionStorage,
                          PersonalWalletStorage personalWalletStorage,
                          LoggingHandler loggingHandler,
                          String... line) {

        super(sessionStorage, personalWalletStorage, loggingHandler, line);
    }

    @Override
    public Optional<String> validate() {
        if (args.length < MIN_LENGTH) {
            return Optional.of("Please enter all arguments");
        }

        try {
            Double.parseDouble(args[AMOUNT]);
        } catch (NumberFormatException e) {
            return Optional.of("Invalid amount entered");
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
            return "user has not logged in";
        }

        try {
            personalWalletStorage.deposit(username, Double.parseDouble(args[1]));

        } catch (NumberFormatException e) {
            log(e, Level.FINE, username);
            return "There was a problem formatting the amount of money.";

        } catch (UserNotFoundException e) {
            log(e, Level.FINE, username);
            return "No user with name: " + username + " could be found";

        } catch (NegativeValueException e) {
            log(e, Level.FINE, username);
            return "Cannot deposit negative amounts of money";
        }

        return "successful deposit";
    }
}
