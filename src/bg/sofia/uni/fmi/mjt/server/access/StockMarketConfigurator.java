package bg.sofia.uni.fmi.mjt.server.access;

import bg.sofia.uni.fmi.mjt.server.access.cache.Cache;
import bg.sofia.uni.fmi.mjt.server.access.cache.ScheduledCache;
import bg.sofia.uni.fmi.mjt.server.dto.Currency;

import java.util.concurrent.TimeUnit;

public class StockMarketConfigurator {
    private static final int DEFAULT_TIME_INTERVAL = 30;
    private static final int DEFAULT_DELAY = 0;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;
    private final int delay;
    private final int timeInterval;
    private final TimeUnit timeUnit;
    private final Cache<String, Currency> currencyCache;

    private StockMarketConfigurator(Builder builder) {
        this.currencyCache = builder.currencyCache;
        this.timeInterval = builder.timeInterval != 0 ? builder.timeInterval : DEFAULT_TIME_INTERVAL;
        this.delay = builder.delay != 0 ? builder.delay : DEFAULT_DELAY;
        this.timeUnit = builder.timeUnit != null ? builder.timeUnit : DEFAULT_TIME_UNIT;
    }

    public static Builder builder(Cache<String, Currency> currencyCache) {
        return new Builder(currencyCache);
    }

    public static class Builder {
        private final Cache<String, Currency> currencyCache;
        private int delay;
        private int timeInterval;
        private TimeUnit timeUnit;

        private Builder(Cache<String, Currency> currencyCache) {
            if (currencyCache == null) {
                this.currencyCache = new ScheduledCache();
            } else {
                this.currencyCache = currencyCache;
            }
        }

        public Builder setDelay(int delay) {
            if (delay > 0) {
                this.delay = delay;
            }
            return this;
        }

        public Builder setTimeInterval(int timeInterval) {
            if (timeInterval > 0) {
                this.timeInterval = timeInterval;
            }
            return this;
        }

        public Builder setTimeUnit(TimeUnit timeUnit) {
            if (timeUnit != null) {
                this.timeUnit = timeUnit;
            }

            return this;
        }

        public StockMarketConfigurator build() {
            return new StockMarketConfigurator(this);
        }

    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public int getDelay() {
        return delay;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public Cache<String, Currency> getCurrencyCache() {
        return currencyCache;
    }

    @Override
    public boolean equals(Object o) {
        return this.currencyCache.equals(((Builder) o).currencyCache);
    }

    @Override
    public int hashCode() {
        return this.currencyCache.hashCode();
    }
}
