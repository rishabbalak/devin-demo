package com.costcodemo.wms.api;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.costcodemo.wms.api.dto.ApiError;
import com.costcodemo.wms.core.service.OrderValidationException;
import com.costcodemo.wms.core.service.RecordNotFoundException;

/**
 * Turns exceptions into the single {@link ApiError} shape.
 *
 * <p>Scoped to the API package so it never intercepts the terminal controller, which
 * renders errors onto line 24 of the screen instead of returning JSON.
 */
@RestControllerAdvice(basePackages = "com.costcodemo.wms.api")
public class GlobalExceptionHandler {

    /** Message id used when the failure did not originate from a core program. */
    private static final String GENERIC_MESSAGE_ID = "USR0001";

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(RecordNotFoundException ex,
                                                   HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessageId(), ex.getMessage(), request);
    }

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<ApiError> handleOrderValidation(OrderValidationException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessageId(), ex.getMessage(), request);
    }

    /**
     * Bean Validation failures. The first field error becomes the message, because the
     * terminal has exactly one message line and the two tiers should say the same thing.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleInvalidBody(MethodArgumentNotValidException ex,
                                                      HttpServletRequest request) {
        String message = "Request is not valid.";
        FieldError first = ex.getBindingResult().getFieldError();
        if (first != null && first.getDefaultMessage() != null) {
            message = first.getDefaultMessage();
        }
        return build(HttpStatus.BAD_REQUEST, GENERIC_MESSAGE_ID, message, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, GENERIC_MESSAGE_ID, ex.getMessage(), request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String messageId,
                                           String message, HttpServletRequest request) {
        ApiError error = ApiError.of(
                status.value(),
                status.getReasonPhrase(),
                messageId,
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(error);
    }
}
