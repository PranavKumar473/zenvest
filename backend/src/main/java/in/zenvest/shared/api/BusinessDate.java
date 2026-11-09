package in.zenvest.shared.api;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/** Standard business date operations for Indian financial markets. Timezone: Asia/Kolkata (IST). */
public final class BusinessDate {

  public static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

  private BusinessDate() {}

  /** Gets today's date in Asia/Kolkata zone using the injected Clock. */
  public static LocalDate today(Clock clock) {
    Objects.requireNonNull(clock, "Clock cannot be null");
    return LocalDate.now(clock.withZone(IST_ZONE));
  }

  /** Returns true if the given date is a weekend (Saturday or Sunday). */
  public static boolean isWeekend(LocalDate date) {
    Objects.requireNonNull(date, "Date cannot be null");
    java.time.DayOfWeek day = date.getDayOfWeek();
    return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
  }

  /** Returns the previous business day (skipping weekends). */
  public static LocalDate previousBusinessDay(LocalDate date) {
    Objects.requireNonNull(date, "Date cannot be null");
    LocalDate candidate = date.minusDays(1);
    while (isWeekend(candidate)) {
      candidate = candidate.minusDays(1);
    }
    return candidate;
  }

  /** Converts a LocalDate to ZonedDateTime at start of day in Asia/Kolkata. */
  public static ZonedDateTime startOfDay(LocalDate date) {
    Objects.requireNonNull(date, "Date cannot be null");
    return date.atStartOfDay(IST_ZONE);
  }
}
