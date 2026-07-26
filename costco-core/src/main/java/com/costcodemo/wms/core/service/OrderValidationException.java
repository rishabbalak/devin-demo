package com.costcodemo.wms.core.service;

/**
 * Raised when an order cannot be accepted. Carries an IBM i style message id so the
 * terminal can render it on line 24 exactly as the RPG programs would.
 */
public class OrderValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String messageId;

    public OrderValidationException(String messageId, String message) {
        super(message);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
