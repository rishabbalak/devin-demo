package com.costcodemo.wms.terminal.session;

/**
 * The attention identifier keys that cause a submit.
 *
 * <p>These are the only events that reach the host. Everything else the operator does —
 * typing, tabbing, arrowing around the screen — happens locally in the terminal and the
 * host never hears about it. When an AID key is pressed the entire screen buffer goes up
 * at once.
 *
 * <p>F3 and F12 are not interchangeable, and mixing them up is the usual tell that
 * somebody has never used one of these systems. F3 leaves the program. F12 backs up one
 * level and keeps the stack.
 */
public enum AidKey {

    ENTER,
    /** Help. */
    F1,
    /** Exit the program entirely. */
    F3,
    /** Prompt for the field under the cursor. */
    F4,
    /** Refresh — reread from the database, discarding uncommitted screen edits. */
    F5,
    /** Create. */
    F6,
    /** Roll down, one page back through a subfile. */
    F7,
    /** Roll up, one page forward through a subfile. */
    F8,
    /** Alternate view — swap the right-hand columns of a subfile. */
    F11,
    /** Cancel — back up one screen. */
    F12,
    /** More keys — cycle the function-key legend. */
    F24;

    public static AidKey parse(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ENTER;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ENTER;
        }
    }
}
