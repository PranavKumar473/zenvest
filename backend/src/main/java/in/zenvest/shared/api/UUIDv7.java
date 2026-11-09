package in.zenvest.shared.api;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Generates UUIDv7 (RFC 9562). UUIDv7 provides time-ordered UUIDs with a millisecond timestamp
 * prefix, preventing B-tree fragmentation in PostgreSQL primary keys.
 */
public final class UUIDv7 {

  private static final SecureRandom RANDOM = new SecureRandom();

  private UUIDv7() {}

  public static UUID randomUUID() {
    return generate(Instant.now().toEpochMilli());
  }

  public static UUID generate(long epochMillis) {
    byte[] value = new byte[16];
    RANDOM.nextBytes(value);

    // 48-bit timestamp
    value[0] = (byte) ((epochMillis >>> 40) & 0xFF);
    value[1] = (byte) ((epochMillis >>> 32) & 0xFF);
    value[2] = (byte) ((epochMillis >>> 24) & 0xFF);
    value[3] = (byte) ((epochMillis >>> 16) & 0xFF);
    value[4] = (byte) ((epochMillis >>> 8) & 0xFF);
    value[5] = (byte) (epochMillis & 0xFF);

    // version 7 (0111)
    value[6] = (byte) ((value[6] & 0x0F) | 0x70);

    // variant 2 (10xx)
    value[8] = (byte) ((value[8] & 0x3F) | 0x80);

    long msb = 0;
    long lsb = 0;
    for (int i = 0; i < 8; i++) {
      msb = (msb << 8) | (value[i] & 0xFF);
    }
    for (int i = 8; i < 16; i++) {
      lsb = (lsb << 8) | (value[i] & 0xFF);
    }

    return new UUID(msb, lsb);
  }
}
