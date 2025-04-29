package com.tjm.common.exceptions;

public class MongoConnectionException extends Exception {
    private static final long serialVersionUID = 1067789734L;
    private String message = null;

    public MongoConnectionException() {
        super();
    }

    public MongoConnectionException(final String message) {
        super(message);
        this.message = message;
    }

    public MongoConnectionException(Throwable cause) {
        super(cause);
    }

    public MongoConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
