package com.ecommerce.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * Unit tests for PriceFormatter.
 */
public class PriceFormatterTest {

    @Test
    public void testFormatUsd() {
        assertEquals("$1,234.50", PriceFormatter.format(new BigDecimal("1234.50"), "USD"));
    }

    @Test
    public void testFormatRoundsHalfUp() {
        assertEquals("$10.56", PriceFormatter.format(new BigDecimal("10.555"), "USD"));
    }

    @Test
    public void testFormatNull() {
        assertEquals("$0.00", PriceFormatter.format(null, "USD"));
    }

    @Test
    public void testSymbolFor() {
        assertEquals("$", PriceFormatter.symbolFor("USD"));
        assertEquals("\u00A3", PriceFormatter.symbolFor("GBP"));
        assertEquals("\u20AC", PriceFormatter.symbolFor("EUR"));
        assertEquals("\u00A5", PriceFormatter.symbolFor("JPY"));
        assertEquals("CAD ", PriceFormatter.symbolFor("CAD"));
        assertEquals("$", PriceFormatter.symbolFor(null));
    }

    @Test
    public void testRound() {
        assertEquals(new BigDecimal("10.56"), PriceFormatter.round(new BigDecimal("10.555")));
        assertEquals(new BigDecimal("0.00"), PriceFormatter.round(null));
    }

    @Test
    public void testPercentOf() {
        assertEquals(new BigDecimal("10.00"), PriceFormatter.percentOf(new BigDecimal("100.00"), new BigDecimal("10")));
        assertEquals(new BigDecimal("25.00"), PriceFormatter.percentOf(new BigDecimal("100.00"), new BigDecimal("25")));
    }

    @Test
    public void testFormatLargeAmount() {
        assertEquals("$1,000,000.00", PriceFormatter.format(new BigDecimal("1000000"), "USD"));
    }
}
