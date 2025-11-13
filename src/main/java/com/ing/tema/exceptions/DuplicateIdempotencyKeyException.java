package com.ing.tema.exceptions;

public class DuplicateIdempotencyKeyException extends RuntimeException {

    public DuplicateIdempotencyKeyException(String key) {
        super("Idempotency key has already been used: " + key);
    }
}
