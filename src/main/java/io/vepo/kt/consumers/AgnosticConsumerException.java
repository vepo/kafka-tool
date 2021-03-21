package io.vepo.kt.consumers;

public class AgnosticConsumerException extends RuntimeException {

    private static final long serialVersionUID = -1819388420385448266L;

    public AgnosticConsumerException(Exception cause) {
        super(cause);
    }
}
