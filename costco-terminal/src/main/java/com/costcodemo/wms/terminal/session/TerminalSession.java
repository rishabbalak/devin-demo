package com.costcodemo.wms.terminal.session;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Per-workstation state: where the operator is, how they got there, and the current
 * position within a subfile.
 *
 * <p>The screen stack is what makes F12 behave correctly. F3 empties it and returns to the
 * menu; F12 pops one entry.
 */
public class TerminalSession implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String SIGN_ON = "SIGNON";
    public static final String MAIN_MENU = "WMSMAIN";

    private String userId = "";
    private String currentScreen = SIGN_ON;
    private String warehouseCode = "W001";
    private final Deque<String> screenStack = new ArrayDeque<>();

    /** Index of the first subfile record shown, for roll up and roll down. */
    private int subfileOffset;

    /** Whether the subfile is showing its alternate column set, toggled by F11. */
    private boolean alternateView;

    /** The order the operator drilled into from the order list, read by WMS311. */
    private String selectedOrderNumber = "";

    private String pendingMessage = "";
    private boolean pendingMessageIsError;

    public String getSelectedOrderNumber() {
        return selectedOrderNumber;
    }

    public void setSelectedOrderNumber(String selectedOrderNumber) {
        this.selectedOrderNumber = selectedOrderNumber == null ? "" : selectedOrderNumber.trim();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId == null ? "" : userId;
    }

    public boolean isSignedOn() {
        return !userId.isEmpty();
    }

    public String getCurrentScreen() {
        return currentScreen;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        if (warehouseCode != null && !warehouseCode.trim().isEmpty()) {
            this.warehouseCode = warehouseCode.trim().toUpperCase();
        }
    }

    public int getSubfileOffset() {
        return subfileOffset;
    }

    public void setSubfileOffset(int subfileOffset) {
        this.subfileOffset = Math.max(subfileOffset, 0);
    }

    public boolean isAlternateView() {
        return alternateView;
    }

    public void toggleAlternateView() {
        this.alternateView = !this.alternateView;
    }

    /** Navigates forward, remembering where we came from so F12 can return. */
    public void pushScreen(String screenId) {
        if (!currentScreen.equals(screenId)) {
            screenStack.push(currentScreen);
            currentScreen = screenId;
            subfileOffset = 0;
        }
    }

    /** F12 — back up one level. Returns to the menu when the stack is empty. */
    public String popScreen() {
        currentScreen = screenStack.isEmpty() ? MAIN_MENU : screenStack.pop();
        subfileOffset = 0;
        return currentScreen;
    }

    /** F3 — leave the program. Clears the stack rather than unwinding it. */
    public void exitToMenu() {
        screenStack.clear();
        currentScreen = MAIN_MENU;
        subfileOffset = 0;
        alternateView = false;
    }

    public void signOff() {
        screenStack.clear();
        userId = "";
        currentScreen = SIGN_ON;
        subfileOffset = 0;
        alternateView = false;
    }

    public void setCurrentScreen(String screenId) {
        this.currentScreen = screenId;
    }

    /**
     * A message that survives exactly one screen transition, mirroring how a program sends
     * a message to the calling program's message queue before returning.
     */
    public void setPendingMessage(String message, boolean isError) {
        this.pendingMessage = message == null ? "" : message;
        this.pendingMessageIsError = isError;
    }

    public String consumePendingMessage() {
        String message = pendingMessage;
        pendingMessage = "";
        return message;
    }

    public boolean isPendingMessageError() {
        return pendingMessageIsError;
    }
}
