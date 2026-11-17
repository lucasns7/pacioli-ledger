package com.lucas.pacioli_ledger.shared.domain.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        try {
            amount = amount.setScale(
                    currency.getPrecision(),
                    RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Amount exceeds the currency's decimal precision",
                    exception
            );
        }
    }

    public static Money zero(Currency currency){
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        validateCurrency(other);

        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        validateCurrency(other);

        return new Money(amount.subtract(other.amount), currency);
    }

    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    public Money abs() {
        return new Money(amount.abs(), currency);
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    public boolean isLessThan(Money other) {
        return compareTo(other) < 0;
    }

    @Override
    public int compareTo(Money other) {
        validateCurrency(other);

        return amount.compareTo(other.amount);
    }

    private void validateCurrency(Money other) {
        Objects.requireNonNull(other, "Money cannot be null");

        if (currency != other.currency) {
            throw new IllegalArgumentException("Cannot operate with different currencies");
        }
    }
}
