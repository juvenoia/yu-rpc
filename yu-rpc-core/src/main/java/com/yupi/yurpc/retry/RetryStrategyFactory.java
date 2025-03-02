package com.yupi.yurpc.retry;

import com.yupi.yurpc.spi.SpiLoader;

public class RetryStrategyFactory {
    static {
        SpiLoader.load(RetryStrategy.class);
    }

    private static final RetryStrategy DEFAULT_RETRY_STRATEGT = new NoRetryStrategy();

    public static RetryStrategy getInstance(String key) {
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }
}
