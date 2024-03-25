package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.exceptions.InsufficientBalanceException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NegativeValueException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Optional;
import java.util.logging.Level;

public final class WithdrawCommand extends CommandBase {
    private static final int MINIMAL_COMMAND_LENGTH = 2;
    public WithdrawCommand(SessionStorage sessionStorage,
                           PersonalWalletStorage personalWalletStorage, LoggingHandler loggingHandler, String... line) {
        super(sessionStorage, personalWalletStorage, loggingHandler, line);
    }

    @Override
    public Optional<String> validate() {
        if (args.length < MINIMAL_COMMAND_LENGTH) {
            return Optional.of("Please provide the amount which you want to withdraw");
        }

        return Optional.empty();
    }

    @Override
    public String execute(SelectionKey selectionKey) {
        if (validate().isPresent()) {
            return validate().get();
        }

        String username = sessionStorage.get(selectionKey);

        try {
            personalWalletStorage.withdraw(username, Double.parseDouble(super.args[1]));
        } catch (NumberFormatException e) {
            log(e, Level.FINE, username);
            return "Please enter correct amount of money";
        } catch (InsufficientBalanceException e) {
            log(e, Level.FINE, username);
            return "There were not enough money in the user's wallet";
        } catch (UserNotFoundException e) {
            log(e, Level.FINE, username);
            return "There has been an error logging in";
        } catch (NegativeValueException e) {
            log(e, Level.FINE, username);
            return "Cannot withdraw negative amount of money";
        }

        return "successful withdrawal";
    }
}
