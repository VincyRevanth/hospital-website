package com.syamalahomeo.hospital.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Simple in-memory login throttling (per IP and per username). */
@Component
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip, String username) {
        return recentFailures("ip:" + ip) >= MAX_FAILURES
                || recentFailures(userKey(username)) >= MAX_FAILURES;
    }

    public void recordFailure(String ip, String username) {
        if (failures.size() > MAX_TRACKED_KEYS) {
            failures.entrySet().removeIf(e -> prune(e.getValue()) == 0);
        }
        add("ip:" + ip);
        add(userKey(username));
    }

    public void reset(String ip, String username) {
        failures.remove("ip:" + ip);
        failures.remove(userKey(username));
    }

    private int recentFailures(String key) {
        Deque<Instant> deque = failures.get(key);
        return deque == null ? 0 : prune(deque);
    }

    private void add(String key) {
        Deque<Instant> deque = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            deque.addLast(Instant.now());
        }
    }

    private int prune(Deque<Instant> deque) {
        Instant cutoff = Instant.now().minus(WINDOW);
        synchronized (deque) {
            while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
                deque.pollFirst();
            }
            return deque.size();
        }
    }

    private static String userKey(String username) {
        String u = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (u.length() > 50) {
            u = u.substring(0, 50);
        }
        return "user:" + u;
    }
}