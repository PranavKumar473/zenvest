package in.zenvest.shared.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BusinessDateTest {

  @Test
  @DisplayName("Identifies weekends correctly")
  void identifiesWeekends() {
    // 2026-11-07 is Saturday, 2026-11-08 is Sunday, 2026-11-09 is Monday
    LocalDate saturday = LocalDate.of(2026, 11, 7);
    LocalDate sunday = LocalDate.of(2026, 11, 8);
    LocalDate monday = LocalDate.of(2026, 11, 9);

    assertThat(BusinessDate.isWeekend(saturday)).isTrue();
    assertThat(BusinessDate.isWeekend(sunday)).isTrue();
    assertThat(BusinessDate.isWeekend(monday)).isFalse();
  }

  @Test
  @DisplayName("Calculates previous business day skipping weekends")
  void calculatesPreviousBusinessDay() {
    LocalDate monday = LocalDate.of(2026, 11, 9);
    LocalDate friday = LocalDate.of(2026, 11, 6);

    assertThat(BusinessDate.previousBusinessDay(monday)).isEqualTo(friday);
    assertThat(BusinessDate.previousBusinessDay(friday)).isEqualTo(LocalDate.of(2026, 11, 5));
  }

  @Test
  @DisplayName("Computes today in Asia/Kolkata timezone")
  void computesTodayInIst() {
    Instant fixedInstant = Instant.parse("2026-11-09T20:00:00Z"); // 20:00 UTC is next day 01:30 IST
    Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

    LocalDate todayIst = BusinessDate.today(fixedClock);
    assertThat(todayIst).isEqualTo(LocalDate.of(2026, 11, 10));
  }
}
