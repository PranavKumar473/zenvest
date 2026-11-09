package in.zenvest.shared.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable Money value object adhering strictly to: - Scale of 2 decimal places. -
 * RoundingMode.HALF_EVEN (Banker's rounding). - Exact mathematical comparisons.
 */
public final class Money implements Comparable<Money>, Serializable {

  public static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_EVEN;
  public static final int DEFAULT_SCALE = 2;

  public static final Money ZERO = new Money(BigDecimal.ZERO);

  private final BigDecimal amount;

  private Money(BigDecimal amount) {
    Objects.requireNonNull(amount, "Amount cannot be null");
    this.amount = amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING);
  }

  public static Money of(BigDecimal amount) {
    return new Money(amount);
  }

  public static Money of(String amountStr) {
    Objects.requireNonNull(amountStr, "Amount string cannot be null");
    return new Money(new BigDecimal(amountStr));
  }

  public static Money of(long wholeUnits) {
    return new Money(BigDecimal.valueOf(wholeUnits));
  }

  public static Money of(double value) {
    return new Money(BigDecimal.valueOf(value));
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Money add(Money other) {
    Objects.requireNonNull(other, "Other money cannot be null");
    return new Money(this.amount.add(other.amount));
  }

  public Money subtract(Money other) {
    Objects.requireNonNull(other, "Other money cannot be null");
    return new Money(this.amount.subtract(other.amount));
  }

  public Money multiply(BigDecimal factor) {
    Objects.requireNonNull(factor, "Factor cannot be null");
    return new Money(this.amount.multiply(factor));
  }

  public Money multiply(long factor) {
    return new Money(this.amount.multiply(BigDecimal.valueOf(factor)));
  }

  public Money divide(BigDecimal divisor) {
    Objects.requireNonNull(divisor, "Divisor cannot be null");
    if (divisor.compareTo(BigDecimal.ZERO) == 0) {
      throw new ArithmeticException("Cannot divide money by zero");
    }
    return new Money(this.amount.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING));
  }

  public boolean isPositive() {
    return this.amount.compareTo(BigDecimal.ZERO) > 0;
  }

  public boolean isNegative() {
    return this.amount.compareTo(BigDecimal.ZERO) < 0;
  }

  public boolean isZero() {
    return this.amount.compareTo(BigDecimal.ZERO) == 0;
  }

  public Money negate() {
    return new Money(this.amount.negate());
  }

  @Override
  public int compareTo(Money other) {
    Objects.requireNonNull(other, "Other money cannot be null");
    return this.amount.compareTo(other.amount);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Money money = (Money) o;
    return this.amount.compareTo(money.amount) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount.stripTrailingZeros());
  }

  @Override
  public String toString() {
    return amount.toPlainString();
  }
}
