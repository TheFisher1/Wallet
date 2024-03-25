package bg.sofia.uni.fmi.mjt.server.access;

import bg.sofia.uni.fmi.mjt.server.access.cache.Cache;
import bg.sofia.uni.fmi.mjt.server.dto.Currency;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StockMarket implements CurrencyObserver {
    private int delay;
    private int timeInterval;
    private TimeUnit timeUnit;
    private Map<String, Currency> currencies;
    private Cache<String, Currency> scheduledCache;
    private ScheduledExecutorService scheduler;

    public StockMarket(List<Currency> currencies) {
        this.currencies = mapAssetIdToCurrency(currencies);
    }

    public StockMarket(StockMarketConfigurator configurator) {
        this.scheduledCache = configurator.getCurrencyCache();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduledCache.addObserver(this);

        this.delay = configurator.getDelay();
        this.timeInterval = configurator.getTimeInterval();
        this.timeUnit = configurator.getTimeUnit();
    }

    public Map<String, Currency> getCurrencies() {
        return currencies;
    }

    public void schedule() {
        scheduler.scheduleAtFixedRate(scheduledCache, delay, timeInterval, timeUnit);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    @Override
    public void update(Map<String, Currency> currencies) {
        this.currencies = currencies;
    }

    private Map<String, Currency> mapAssetIdToCurrency(List<Currency> currencies) {
        return Map.copyOf(currencies.stream()
            .collect(Collectors.toMap(Currency::assetId, Function.identity())));
    }

}
