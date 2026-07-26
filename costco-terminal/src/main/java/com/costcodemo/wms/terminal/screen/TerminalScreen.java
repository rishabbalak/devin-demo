package com.costcodemo.wms.terminal.screen;

import java.util.ArrayList;
import java.util.List;

/**
 * One rendered screen: the character buffer, the input fields laid over it, and the
 * message line.
 */
public class TerminalScreen {

    private final String screenId;
    private final ScreenBuffer buffer = new ScreenBuffer();
    private final List<ScreenField> fields = new ArrayList<>();

    private String message = "";
    private boolean messageIsError;
    private String cursorField;

    public TerminalScreen(String screenId) {
        this.screenId = screenId;
    }

    public String getScreenId() {
        return screenId;
    }

    public ScreenBuffer getBuffer() {
        return buffer;
    }

    public List<ScreenField> getFields() {
        return fields;
    }

    public ScreenField addField(ScreenField field) {
        fields.add(field);
        return field;
    }

    public ScreenField findField(String name) {
        for (ScreenField field : fields) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    public String getMessage() {
        return message;
    }

    public boolean isMessageIsError() {
        return messageIsError;
    }

    /**
     * Sets the line-24 message. Errors additionally put the named field into reverse image
     * and park the cursor on it, which is what the RPG programs do via {@code DSPATR(RI PC)}.
     */
    public void setError(String text, String fieldInError) {
        this.message = text == null ? "" : text;
        this.messageIsError = true;
        if (fieldInError != null) {
            ScreenField field = findField(fieldInError);
            if (field != null) {
                buffer.setReverse(field.getRow(), field.getCol(), field.getLength());
                this.cursorField = fieldInError;
            }
        }
    }

    /** An informational message. Renders without reverse image and without the alarm. */
    public void setInfo(String text) {
        this.message = text == null ? "" : text;
        this.messageIsError = false;
    }

    public String getCursorField() {
        return cursorField;
    }

    public void setCursorField(String cursorField) {
        this.cursorField = cursorField;
    }
}
