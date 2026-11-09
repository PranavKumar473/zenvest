package in.zenvest.shared.internal;

import in.zenvest.shared.api.RateLimiterService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class Bucket4jRateLimiterService implements RateLimiterService {

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Override
  public boolean tryConsume(String key, int tokens, int capacity, Duration refillDuration) {
    Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(capacity, refillDuration));
    return bucket.tryConsume(tokens);
  }

  private Bucket createBucket(int capacity, Duration refillDuration) {
    Refill refill = Refill.greedy(capacity, refillDuration);
    Bandwidth limit = Bandwidth.classic(capacity, refill);
    return Bucket.builder().addLimit(limit).build();
  }
}
