package com.ecommerce.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates a Throwable originating from a stored-procedure SIGNAL into a
 * specific application exception, based on the message-text prefix convention
 * used in procedures.sql.
 *
 * Stored procedures raise business rule violations with:
 *   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'PREFIX: human readable detail';
 *
 * This translator inspects the message and maps the prefix to a typed
 * exception so controllers never see a raw SQLException.
 */
public final class SqlErrorTranslator {

    private static final Logger logger = LoggerFactory.getLogger(SqlErrorTranslator.class);

    private SqlErrorTranslator() {
    }

    public static EcommerceException translate(Throwable ex) {
        String message = extractMessage(ex);
        logger.debug("Translating DB error: {}", message);

        if (message == null) {
            return new EcommerceException("DB_ERROR", "A database error occurred", ex);
        }

        String upper = message.toUpperCase();
        if (upper.contains("INSUFFICIENT_STOCK")) {
            return new InsufficientStockException(cleanMessage(message));
        }
        if (upper.contains("DISCOUNT_INVALID")) {
            return new DiscountInvalidException(cleanMessage(message));
        }
        if (upper.contains("INVALID_STATUS")) {
            return new InvalidStatusTransitionException(cleanMessage(message));
        }
        if (upper.contains("NOT_FOUND")) {
            return new ResourceNotFoundException(cleanMessage(message));
        }
        if (upper.contains("PAYMENT_ERROR")) {
            return new PaymentException(cleanMessage(message));
        }
        if (upper.contains("INVALID_AMOUNT")) {
            return new InvalidAmountException(cleanMessage(message));
        }
        return new EcommerceException("DB_ERROR", cleanMessage(message), ex);
    }

    private static String extractMessage(Throwable ex) {
        Throwable current = ex;
        String best = null;
        int guard = 0;
        while (current != null && guard < 20) {
            String m = current.getMessage();
            if (m != null && containsKnownPrefix(m)) {
                return m;
            }
            if (m != null) {
                best = m;
            }
            current = current.getCause();
            guard++;
        }
        return best;
    }

    private static boolean containsKnownPrefix(String m) {
        String u = m.toUpperCase();
        return u.contains("INSUFFICIENT_STOCK") || u.contains("DISCOUNT_INVALID")
                || u.contains("INVALID_STATUS") || u.contains("NOT_FOUND")
                || u.contains("PAYMENT_ERROR") || u.contains("INVALID_AMOUNT");
    }

    private static String cleanMessage(String message) {
        int idx = message.indexOf(':');
        if (idx > 0 && idx < 40) {
            return message.substring(idx + 1).trim();
        }
        return message;
    }
}
