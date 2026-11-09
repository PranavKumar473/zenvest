package in.zenvest.shared.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UUIDv7Test {

  @Test
  @DisplayName("Generates valid RFC 9562 UUIDv7")
  void generatesValidUuidV7() {
    UUID uuid = UUIDv7.randomUUID();
    assertThat(uuid).isNotNull();
    // Version 7
    assertThat(uuid.version()).isEqualTo(7);
    // Variant 2 (Leach-Salz / RFC 4122/9562)
    assertThat(uuid.variant()).isEqualTo(2);
  }

  @Test
  @DisplayName("UUIDv7 is monotonic and time-ordered")
  void isTimeOrdered() throws InterruptedException {
    UUID u1 = UUIDv7.randomUUID();
    Thread.sleep(5);
    UUID u2 = UUIDv7.randomUUID();

    assertThat(u1.compareTo(u2)).isLessThan(0);
  }
}
