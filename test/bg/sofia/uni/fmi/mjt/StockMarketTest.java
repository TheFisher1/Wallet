package bg.sofia.uni.fmi.mjt;

import bg.sofia.uni.fmi.mjt.server.access.StockMarket;
import bg.sofia.uni.fmi.mjt.server.access.StockMarketConfigurator;
import bg.sofia.uni.fmi.mjt.server.access.cache.ScheduledCache;
import bg.sofia.uni.fmi.mjt.server.access.retrieve.CurrencyRetriever;
import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.exceptions.CurrencyRetrievalException;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StockMarketTest {
    @Mock
    HttpClient httpClient;
    @Mock
    HttpResponse<Object> httpResponse;
    @Mock
    ScheduledCache scheduledCache;

    private static final int SUCCESS = 200;
    private static final int FAIL = 400;
    private static final Gson GSON = new Gson();

    private static List<Currency> currencyList = new ArrayList<>();


    @BeforeAll
    static void setUp() {
        for (int i = 0; i < 5; ++i) {
            currencyList.add(new Currency("assetId"+i, "name" + i, i, i % 2));
        }

    }

    @Test
    void testGetCurrencies() throws IOException, InterruptedException {
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(SUCCESS);
        when(httpResponse.body()).thenReturn(GSON.toJson(currencyList));

        List<Currency> currencies = CurrencyRetriever.retrieveResources(httpClient);
        assertTrue(currencies.containsAll(currencyList) && currencyList.containsAll(currencies));
    }

    @Test
    void testGetCurrenciesUnsuccessfulOperation() throws IOException, InterruptedException {
        when(httpClient.send(any(), any())).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(FAIL);

        assertThrows(CurrencyRetrievalException.class, () -> CurrencyRetriever.retrieveResources(httpClient));
    }

    @Test
    void testCacheSendsRequestsAppropriately() throws InterruptedException {

        StockMarket stockMarket = new StockMarket(StockMarketConfigurator.builder(scheduledCache).setDelay(0).setTimeInterval(10).setTimeUnit(TimeUnit.MILLISECONDS).build());
        stockMarket.schedule();
        Thread.sleep(50);
        verify(scheduledCache, atLeast(4)).run();
    }

    @Test
    void testCurrencyRetrieverThrows() throws IOException, InterruptedException {
        when(httpClient.send(any(), any())).thenThrow(IOException.class);

        assertThrows(CurrencyRetrievalException.class, () -> CurrencyRetriever.retrieveResources(httpClient));
    }


}
