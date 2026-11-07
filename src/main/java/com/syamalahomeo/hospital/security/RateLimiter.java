package com.syamalahomeo.hospital.security;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory sliding-window limiter. Fine for one server; use a shared store if you ever scale out. */
public class RateLimiter {

    private static final int MAX_KEYS = 10_000;

    private final int max;
    private final Duration window;
    private final Map<String, Deque<Instant>> hits = new ConcurrentHashMap<>();

    public RateLimiter(int max, Duration window) {
        this.max = max;
        this.window = window;
    }

    public boolean tryAcquire(String key) {
        if (hits.size() > MAX_KEYS) {
            hits.entrySet().removeIf(e -> prune(e.getValue()) == 0);
        }
        Deque<Instant> deque = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            prune(deque);
            if (deque.size() >= max) {
                return false;
            }
            deque.addLast(Instant.now());
            return true;
        }
    }

    public long windowSeconds() {
        return window.toSeconds();
    }

    private int prune(Deque<Instant> deque) {
        Instant cutoff = Instant.now().minus(window);
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
                deque.pollFirst();
            }
            return deque.size();
        }
    }
}