package in.zenvest.shared.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IdempotencyRecordTest {

  @Test
  @DisplayName("Creates idempotency record and reads fields")
  void createsRecord() {
    IdempotencyRecord record =
        new IdempotencyRecord("user-1", "key-123", "hash-abc", 200, "{\"success\":true}");

    assertThat(record.getUserId()).isEqualTo("user-1");
    assertThat(record.getIdempotencyKey()).isEqualTo("key-123");
    assertThat(record.getRequestHash()).isEqualTo("hash-abc");
    assertThat(record.getResponseStatus()).isEqualTo(200);
    assertThat(record.getResponseBody()).isEqualTo("{\"success\":true}");
    assertThat(record.getCreatedAt()).isNotNull();
  }
}
