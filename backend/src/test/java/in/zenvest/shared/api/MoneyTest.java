package in.zenvest.shared.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

  @Test
  @DisplayName("Money maintains scale of 2 with HALF_EVEN rounding")
  void maintainsScaleAndHalfEvenRounding() {
    // 2.555 -> 2.56 (rounds to nearest even)
    assertThat(Money.of("2.555").getAmount()).isEqualByComparingTo("2.56");
    // 2.545 -> 2.54 (rounds to nearest even: 4 is even)
    assertThat(Money.of("2.545").getAmount()).isEqualByComparingTo("2.54");
    // 100 -> 100.00
    assertThat(Money.of("100").getAmount()).isEqualByComparingTo("100.00");
  }

  @Test
  @DisplayName("Arithmetic operations: add, subtract, multiply, divide")
  void arithmeticOperations() {
    Money m1 = Money.of("150.50");
    Money m2 = Money.of("49.50");

    assertThat(m1.add(m2)).isEqualTo(Money.of("200.00"));
    assertThat(m1.subtract(m2)).isEqualTo(Money.of("101.00"));
    assertThat(Money.of("50.00").multiply(2)).isEqualTo(Money.of("100.00"));
    assertThat(Money.of("100.00").divide(new BigDecimal("3"))).isEqualTo(Money.of("33.33"));
  }

  @Test
  @DisplayName("Division by zero throws ArithmeticException")
  void divisionByZeroThrows() {
    Money m = Money.of("100.00");
    assertThatThrownBy(() -> m.divide(BigDecimal.ZERO))
        .isInstanceOf(ArithmeticException.class)
        .hasMessageContaining("Cannot divide money by zero");
  }

  @Test
  @DisplayName("Comparisons and status checks work accurately")
  void statusAndComparisons() {
    Money positive = Money.of("10.00");
    Money zero = Money.ZERO;
    Money negative = Money.of("-10.00");

    assertThat(positive.isPositive()).isTrue();
    assertThat(zero.isZero()).isTrue();
    assertThat(negative.isNegative()).isTrue();

    assertThat(positive.compareTo(zero)).isGreaterThan(0);
    assertThat(negative.compareTo(zero)).isLessThan(0);
    assertThat(Money.of("10.0").equals(Money.of("10.00"))).isTrue();
  }
}
