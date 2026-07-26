package com.costcodemo.wms.terminal.screen;

/**
 * An input-capable field on the screen.
 *
 * <p>Fields render as underscores and sit at a fixed grid position with a fixed length,
 * exactly as a DDS-defined input field does. The buffer underneath them is left blank so
 * the field is the only thing occupying those cells.
 */
public class ScreenField {

    private final String name;
    private final int row;
    private final int col;
    private final int length;
    private final ScreenColor color;
    private final boolean numeric;
    private String value;

    public ScreenField(String name, int row, int col, int length, String value) {
        this(name, row, col, length, value, ScreenColor.WHITE, false);
    }

    public ScreenField(String name, int row, int col, int length, String value,
                       ScreenColor color, boolean numeric) {
        this.name = name;
        this.row = row;
        this.col = col;
        this.length = length;
        this.value = value == null ? "" : value;
        this.color = color;
        this.numeric = numeric;
    }

    public String getName() {
        return name;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getLength() {
        return length;
    }

    public ScreenColor getColor() {
        return color;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value == null ? "" : value;
    }

    public String getCssClass() {
        return "c-" + color.getCssSuffix();
    }

    /** Pixel offset from the top of the grid, using the 16px cell height. */
    public int getTopPx() {
        return (row - 1) * 16;
    }
}
