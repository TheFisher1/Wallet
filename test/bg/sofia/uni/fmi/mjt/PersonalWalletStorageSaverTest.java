package bg.sofia.uni.fmi.mjt;

import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorage;
import bg.sofia.uni.fmi.mjt.server.datastore.PersonalWalletStorageSaver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PersonalWalletStorageSaverTest {
    private PersonalWalletStorage personalWalletStorage;

    @Test
    void testSaverGetsScheduledAsExpected() throws InterruptedException {
        personalWalletStorage = mock(PersonalWalletStorage.class);
        PersonalWalletStorageSaver saver = new PersonalWalletStorageSaver(personalWalletStorage);

        saver.schedule(0, 8, TimeUnit.MILLISECONDS);

        Thread.sleep(50);
        verify(personalWalletStorage, (atLeast(4))).run();
    }
}
