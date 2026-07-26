package com.costcodemo.wms.core.service;

/**
 * Raised when a keyed read finds no record.
 *
 * <p>The message is deliberately shaped like an IBM i message-file entry, because it is
 * rendered verbatim on line 24 of the terminal.
 */
public class RecordNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String messageId;

    public RecordNotFoundException(String messageId, String message) {
        super(message);
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }
}
