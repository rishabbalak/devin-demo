package com.costcodemo.wms.terminal.screen;

/**
 * The seven colours a 5250 colour terminal can display.
 *
 * <p>On real hardware these were never independent colour codes. The 5250 datastream
 * carries a monochrome attribute byte, and colour terminals such as the 3179 and 3180
 * re-purposed its existing bits: high-intensity became white, blink became red, column
 * separator became turquoise, and combinations produced the rest. The mapping is recorded
 * on each constant because it explains why this particular set of seven exists and why
 * there is no orange, grey, or black-on-white.
 */
public enum ScreenColor {

    /** No attribute bits set. Normal output text. */
    GREEN("grn"),

    /** High intensity. Input-capable fields and key data. */
    WHITE("wht"),

    /** Blink. Errors, exception statuses, negative quantities. */
    RED("red"),

    /** Column separator. Column headings and field prompts. */
    TURQUOISE("trq"),

    /** Column separator plus high intensity. Screen titles and totals. */
    YELLOW("ylw"),

    /** Blink plus column separator. Informational, non-error messages. */
    PINK("pnk"),

    /** Blink, column separator and high intensity. Function-key legend, separators. */
    BLUE("blu");

    private final String cssSuffix;

    ScreenColor(String cssSuffix) {
        this.cssSuffix = cssSuffix;
    }

    /** Suffix of the CSS class used to render this colour, e.g. {@code c-grn}. */
    public String getCssSuffix() {
        return cssSuffix;
    }
}
