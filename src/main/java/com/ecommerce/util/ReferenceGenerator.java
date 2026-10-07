package com.ecommerce.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Static utility for generating reference codes (legacy-style helper).
 */
public final class ReferenceGenerator {

    private ReferenceGenerator() {
    }

    public static String customerRef() {
        return "C" + pad(ThreadLocalRandom.current().nextInt(1, 9999999), 7);
    }

    public static String sku() {
        return "SKU-" + pad(ThreadLocalRandom.current().nextInt(1, 99999), 5);
    }

    public static String transactionId() {
        return "TXN" + pad(ThreadLocalRandom.current().nextInt(1, 99999999), 8);
    }

    public static String trackingNumber() {
        return "1Z" + pad(ThreadLocalRandom.current().nextInt(1, 999999999), 10);
    }

    private static String pad(int value, int width) {
        String s = String.valueOf(value);
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < width; i++) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }
}
