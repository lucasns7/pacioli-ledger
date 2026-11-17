package com.lucas.pacioli_ledger.shared.domain.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithCorrectScale() {
        Money money = new Money(
                new BigDecimal("100"),
                Currency.BRL
        );

        assertEquals(new BigDecimal("100.00"), money.amount());
    }

    @Test
    void shouldRejectAmountWithExcessDecimalPlaces() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Money(
                        new BigDecimal("10.555"),
                        Currency.BRL
                )
        );
    }

    @Test
    void shouldAddMoneyWithSameCurrency() {
        Money first = new Money(
                new BigDecimal("100.00"),
                Currency.BRL
        );

        Money second = new Money(
                new BigDecimal("50.00"),
                Currency.BRL
        );

        Money result = first.add(second);

        assertEquals(new BigDecimal("150.00"), result.amount());
    }

    @Test
    void shouldSubtractMoneyWithSameCurrency() {
        Money first = new Money(
                new BigDecimal("100.00"),
                Currency.BRL
        );

        Money second = new Money(
                new BigDecimal("30.00"),
                Currency.BRL
        );

        Money result = first.subtract(second);

        assertEquals(new BigDecimal("70.00"), result.amount());
    }

    @Test
    void shouldAllowNegativeResults() {
        Money first = new Money(
                new BigDecimal("10.00"),
                Currency.BRL
        );

        Money second = new Money(
                new BigDecimal("20.00"),
                Currency.BRL
        );

        Money result = first.subtract(second);

        assertTrue(result.isNegative());
        assertEquals(new BigDecimal("-10.00"), result.amount());
    }

    @Test
    void shouldIdentifyZeroAmount() {
        Money money = Money.zero(Currency.BRL);

        assertTrue(money.isZero());
        assertFalse(money.isPositive());
        assertFalse(money.isNegative());
    }

    @Test
    void shouldCompareMoneyValues() {
        Money first = new Money(
                new BigDecimal("150.00"),
                Currency.BRL
        );

        Money second = new Money(
                new BigDecimal("100.00"),
                Currency.BRL
        );

        assertTrue(first.isGreaterThan(second));
        assertTrue(second.isLessThan(first));
    }

    @Test
    void shouldNotModifyOriginalMoneyWhenAdding() {
        Money original = new Money(
                new BigDecimal("100.00"),
                Currency.BRL
        );

        original.add(new Money(
                new BigDecimal("50.00"),
                Currency.BRL
        ));

        assertEquals(new BigDecimal("100.00"), original.amount());
    }
}