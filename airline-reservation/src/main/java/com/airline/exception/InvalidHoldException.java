package com.airline.exception;

public class InvalidHoldException extends RuntimeException {
    public InvalidHoldException(String message) {
        super(message);
    }
}
