package in.zenvest.shared.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Bucket4jRateLimiterServiceTest {

  private final Bucket4jRateLimiterService rateLimiterService = new Bucket4jRateLimiterService();

  @Test
  @DisplayName("Allows requests within capacity and blocks when exceeded")
  void allowsAndBlocksProperly() {
    String key = "test-ip-127.0.0.1";
    int capacity = 3;
    Duration refill = Duration.ofMinutes(1);

    // 3 tokens allowed
    assertThat(rateLimiterService.tryConsume(key, 1, capacity, refill)).isTrue();
    assertThat(rateLimiterService.tryConsume(key, 1, capacity, refill)).isTrue();
    assertThat(rateLimiterService.tryConsume(key, 1, capacity, refill)).isTrue();

    // 4th token blocked
    assertThat(rateLimiterService.tryConsume(key, 1, capacity, refill)).isFalse();
  }
}
