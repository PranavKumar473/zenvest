package in.zenvest.shared.api;

public interface RateLimiterService {
  boolean tryConsume(String key, int tokens, int capacity, java.time.Duration refillDuration);
}
