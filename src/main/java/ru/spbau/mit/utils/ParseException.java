package ru.spbau.mit.utils;

public class ParseException extends Exception {
    ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
