package mx.decants.config;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_MS = 15 * 60 * 1000L;

    private final Map<String, Attempt> cache = new ConcurrentHashMap<>();

    public void failed(String ip) {
        Attempt attempt = cache.computeIfAbsent(ip, k -> new Attempt());
        attempt.increment();
    }

    public void succeeded(String ip) {
        cache.remove(ip);
    }

    public boolean isBlocked(String ip) {
        Attempt attempt = cache.get(ip);
        if (attempt == null) return false;
        if (attempt.count < MAX_ATTEMPTS) return false;
        if (System.currentTimeMillis() - attempt.lastTime > BLOCK_MS) {
            cache.remove(ip);
            return false;
        }
        return true;
    }

    private static class Attempt {
        int count;
        long lastTime;

        synchronized void increment() {
            count++;
            lastTime = System.currentTimeMillis();
        }
    }
}
