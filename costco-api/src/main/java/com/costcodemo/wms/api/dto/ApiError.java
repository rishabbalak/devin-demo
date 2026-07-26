package com.costcodemo.wms.api.dto;

import java.time.OffsetDateTime;

/**
 * The single error shape returned by every endpoint.
 *
 * <p>{@code messageId} carries the IBM i message identifier the core would have signalled,
 * so an API consumer and an operator reading line 24 of a green screen are looking at the
 * same diagnostic.
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String messageId,
        String message,
        String path) {

    public static ApiError of(int status, String error, String messageId,
                              String message, String path) {
        return new ApiError(OffsetDateTime.now(), status, error, messageId, message, path);
    }
}
