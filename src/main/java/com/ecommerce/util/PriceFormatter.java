package com.ecommerce.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Static utility class for price/currency formatting.
 *
 * NOTE (legacy smell): classic static utility holder with a private
 * constructor and a shared, non-thread-safe DecimalFormat guarded by
 * synchronized blocks, plus a hand-rolled currency-symbol switch. Kept
 * deliberately as a representative legacy pattern.
 */
public final class PriceFormatter {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0.00");

    private PriceFormatter() {
    }

    public static synchronized String format(BigDecimal amount, String currency) {
        if (amount == null) {
            return symbolFor(currency) + "0.00";
        }
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        return symbolFor(currency) + MONEY_FORMAT.format(scaled);
    }

    public static String format(BigDecimal amount) {
        return format(amount, "USD");
    }

    public static String symbolFor(String currency) {
        if (currency == null) {
            return "$";
        }
        switch (currency.toUpperCase()) {
            case "USD": return "$";
            case "GBP": return "\u00A3";
            case "EUR": return "\u20AC";
            case "JPY": return "\u00A5";
            default: return currency.toUpperCase() + " ";
        }
    }

    public static BigDecimal round(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /** Compute a percentage of an amount, rounded to 2dp. */
    public static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
        if (amount == null || percent == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.multiply(percent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
