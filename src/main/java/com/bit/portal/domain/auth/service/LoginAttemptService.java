package com.bit.portal.domain.auth.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 5 * 60 * 1000L; // 5분

    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lockUntil = new ConcurrentHashMap<>();

    public void loginFailed(String email) {
        int count = attempts.merge(email, 1, Integer::sum);
        if (count >= MAX_ATTEMPTS) {
            lockUntil.put(email, System.currentTimeMillis() + LOCK_DURATION_MS);
            attempts.remove(email);
        }
    }

    public void loginSucceeded(String email) {
        attempts.remove(email);
        lockUntil.remove(email);
    }

    public boolean isLocked(String email) {
        Long until = lockUntil.get(email);
        if (until == null) return false;
        if (System.currentTimeMillis() < until) return true;
        lockUntil.remove(email); // 잠금 시간 경과 → 자동 해제
        return false;
    }
}
