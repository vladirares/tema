package com.ing.tema.dtos;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String errorCode,
        List<String> details
) {
    public static ErrorResponse of(
            int status,
            String error,
            String message,
            String path,
            String errorCode,
            List<String> details
    ) {
        return new ErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                path,
                errorCode,
                details
        );
    }

    public static ErrorResponse of(
            int status,
            String error,
            String message,
            String path,
            String errorCode
    ) {
        return of(status, error, message, path, errorCode, List.of());
    }
}
