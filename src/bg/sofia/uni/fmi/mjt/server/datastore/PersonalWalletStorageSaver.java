package bg.sofia.uni.fmi.mjt.server.datastore;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PersonalWalletStorageSaver {
    private static final int TIME_INTERVAL = 30;
    private static final int DELAY = 0;
    private final PersonalWalletStorage personalWalletStorage;
    private ScheduledExecutorService autoSaver;
    public PersonalWalletStorageSaver(PersonalWalletStorage personalWalletStorage) {
        this.personalWalletStorage = personalWalletStorage;
    }

    public void schedule() {
        schedule(DELAY, TIME_INTERVAL, TimeUnit.MINUTES);
    }

    public void schedule(int delay, int timeInterval, TimeUnit timeUnit) {
        autoSaver = Executors.newSingleThreadScheduledExecutor();
        autoSaver.scheduleWithFixedDelay(personalWalletStorage, delay, timeInterval, timeUnit);
    }

    public void shutdown() {
        autoSaver.shutdown();
    }
}
