package com.costcodemo.wms.terminal.screen;

import java.util.Arrays;

/**
 * A 24x80 character buffer, the exact geometry of {@code DSPSIZ(24 80 *DS3)}.
 *
 * <p>Rows and columns are 1-based, matching how positions are written in DDS source.
 *
 * <h2>The attribute cell</h2>
 * On a 5250 a display attribute is not metadata attached to a character — it occupies a
 * screen position of its own and renders as a blank. That is why every colour change on a
 * real green screen is preceded by exactly one blank column, and why nothing ever butts up
 * against a differently-coloured region.
 *
 * <p>{@link #write} reproduces this: writing text at column {@code c} also claims column
 * {@code c - 1} as the attribute position and blanks it. Screen layouts therefore have to
 * leave a gap between adjacent coloured fields — the same constraint a DDS author worked
 * under. Skipping this is the single subtlest way a recreation gives itself away.
 */
public final class ScreenBuffer {

    public static final int ROWS = 24;
    public static final int COLS = 80;

    private final char[][] characters = new char[ROWS][COLS];
    private final ScreenColor[][] colors = new ScreenColor[ROWS][COLS];
    private final boolean[][] reverse = new boolean[ROWS][COLS];

    public ScreenBuffer() {
        clear();
    }

    public void clear() {
        for (int row = 0; row < ROWS; row++) {
            Arrays.fill(characters[row], ' ');
            Arrays.fill(colors[row], ScreenColor.GREEN);
            Arrays.fill(reverse[row], false);
        }
    }

    /**
     * Writes text at a 1-based position, claiming the preceding column as the attribute
     * cell. Text running past column 80 is truncated rather than wrapped — a 5250 buffer
     * has no concept of reflow.
     */
    public void write(int row, int col, String text, ScreenColor color) {
        if (text == null || row < 1 || row > ROWS) {
            return;
        }
        int r = row - 1;

        if (col > 1) {
            int attributeCol = col - 2;
            if (attributeCol >= 0 && attributeCol < COLS) {
                characters[r][attributeCol] = ' ';
                colors[r][attributeCol] = color;
                reverse[r][attributeCol] = false;
            }
        }

        for (int i = 0; i < text.length(); i++) {
            int c = (col - 1) + i;
            if (c < 0) {
                continue;
            }
            if (c >= COLS) {
                break;
            }
            characters[r][c] = text.charAt(i);
            colors[r][c] = color;
        }
    }

    /** Writes text centred across the 80-column line. */
    public void writeCentered(int row, String text, ScreenColor color) {
        if (text == null) {
            return;
        }
        int col = ((COLS - text.length()) / 2) + 1;
        write(row, Math.max(col, 1), text, color);
    }

    /** Writes text so that its last character lands on {@code endCol}. */
    public void writeRight(int row, int endCol, String text, ScreenColor color) {
        if (text == null) {
            return;
        }
        write(row, Math.max(endCol - text.length() + 1, 1), text, color);
    }

    /**
     * Turns a run of cells to reverse image, which is how a field in error is marked.
     * Applied on top of whatever colour is already there.
     */
    public void setReverse(int row, int col, int length) {
        if (row < 1 || row > ROWS) {
            return;
        }
        int r = row - 1;
        for (int i = 0; i < length; i++) {
            int c = (col - 1) + i;
            if (c >= 0 && c < COLS) {
                reverse[r][c] = true;
            }
        }
    }

    public char characterAt(int row, int col) {
        return characters[row - 1][col - 1];
    }

    public ScreenColor colorAt(int row, int col) {
        return colors[row - 1][col - 1];
    }

    public boolean isReverseAt(int row, int col) {
        return reverse[row - 1][col - 1];
    }

    /** The full text of one row, spaces included. Used by tests to assert on layout. */
    public String rowText(int row) {
        return new String(characters[row - 1]);
    }

    /** All 1920 characters, row by row, with no separators. */
    public String snapshot() {
        StringBuilder builder = new StringBuilder(ROWS * COLS);
        for (int row = 1; row <= ROWS; row++) {
            builder.append(rowText(row));
        }
        return builder.toString();
    }
}
