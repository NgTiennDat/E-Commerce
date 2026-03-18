package com.eCommerce.auth.service;

import com.eCommerce.common.exception.CustomException;
import com.eCommerce.common.payload.ResponseCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Very lightweight fixed-window rate limiter for login/refresh to prevent brute force.
 * In production, move to gateway or use a distributed limiter (Redis/Bucket4j).
 */
@Component
public class RateLimiterService {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MILLIS = 5 * 60 * 1000; // 5 minutes

    private static final class Counter {
        long windowStart;
        AtomicInteger attempts = new AtomicInteger(0);
    }

    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public void assertAllowed(String key) {
        long now = Instant.now().toEpochMilli();
        Counter counter = counters.computeIfAbsent(key, k -> {
            Counter c = new Counter();
            c.windowStart = now;
            return c;
        });

        synchronized (counter) {
            if (now - counter.windowStart > WINDOW_MILLIS) {
                counter.windowStart = now;
                counter.attempts.set(0);
            }

            if (counter.attempts.incrementAndGet() > MAX_ATTEMPTS) {
                throw new CustomException(ResponseCode.INVALID_REQUEST);
            }
        }
    }
}
