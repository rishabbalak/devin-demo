package com.costcodemo.wms.terminal.screen;

import com.costcodemo.wms.terminal.session.TerminalInput;
import com.costcodemo.wms.terminal.session.TerminalSession;

/**
 * One program in the WMS application. Each handler owns a single screen: it builds the
 * display buffer and it decides what a submit means.
 *
 * <p>This mirrors how the real programs are organised — one RPG program per display file,
 * with the program name and the screen id being the same thing.
 */
public interface ScreenHandler {

    /** The screen id, shown at the top left and used to route submits. */
    String screenId();

    /** Builds the screen as the operator should currently see it. */
    TerminalScreen render(TerminalSession session);

    /**
     * Processes a whole-screen submit, mutating the session to navigate or to leave a
     * message for the next render.
     */
    void handle(TerminalSession session, TerminalInput input);
}
