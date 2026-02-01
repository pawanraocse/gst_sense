package com.learning.backendservice.exception;

/**
 * Thrown when ledger file parsing fails (invalid format, missing columns, etc.).
 */
public class LedgerParseException extends RuntimeException {

    public LedgerParseException(String message) {
        super(message);
    }

    public LedgerParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
