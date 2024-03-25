package bg.sofia.uni.fmi.mjt.server.commands.hierarchy;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.SessionStorage;
import bg.sofia.uni.fmi.mjt.server.logging.LoggingHandler;

import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;

public abstract sealed class CommandBase
                     permits BalanceCommand,
                             BuyAssetCommand,
                             DepositCommand,
                             EmptyCommand,
                             GetOverallSummaryCommand,
                             GetSummaryCommand,
                             ListAssetsCommand,
                             ListOfferingsCommand,
                             LogInCommand,
                             LogOutCommand,
                             RegisterCommand,
                             SellAssetCommand,
                             UnknownCommand,
                             WithdrawCommand {
    protected static final String FORMAT_VALUE = "%.3f";

    protected SessionStorage sessionStorage;
    protected PersonalWalletStorage personalWalletStorage;
    protected String[] args;
    protected LoggingHandler loggingHandler;

    public CommandBase(SessionStorage sessionStorage,
                       PersonalWalletStorage personalWalletStorage,
                       LoggingHandler loggingHandler,
                       String... line) {

        this.personalWalletStorage = personalWalletStorage;
        this.sessionStorage = sessionStorage;
        this.args = line;
        this.loggingHandler = loggingHandler;
    }

    public abstract Optional<String> validate();

    public abstract String execute(SelectionKey selectionKey);

    public String printValidCommands() {
        return """
            register <username> <password> - registers a new user with <username> and <password>          
            log-in <username> <password> - logs the user in
            log-out - logs the user out
            deposit <amount $> - adds <amount $> to the user's profile
            withdraw <amount $> - withdraws <amount $> from the user's profile
            buy-asset <amount $> <asset_id> - acquires <amount $> of asset with the <asset_id>
            sell-asset <asset_id> - sell all assets that are possessed by the user and have <asset_id>
            list-assets - prints all assets currently owned by the user
            list-offerings - prints all offered currencies
            get-summary  - summarises the currently owned portfolio of <username>
            get-overall-summary - summarises the portfolio of its owner for all its existence;
            """;
    }

    protected void log(Exception e, Level level, String user) {
        loggingHandler.log(level, "there was a problem: " + e.getMessage()
            + ". Stacktrace: " + Arrays.toString(e.getStackTrace())
            +   "was caused by " + user);
    }

}
