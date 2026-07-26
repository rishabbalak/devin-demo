package com.costcodemo.wms.terminal.screen;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

/**
 * Draws the parts of a screen that never change: the header band, the function-key legend,
 * and the message line.
 *
 * <p>The layout follows the house standard every panel on this system uses — program id at
 * the top left, title centred, system name at the right, date and time beneath it, the
 * F-key legend on line 23 and messages on line 24.
 */
@Component
public class ScreenChrome {

    /** The message subfile is declared {@code SFLMSGRCD(24)}, so messages land on line 24. */
    public static final int MESSAGE_ROW = 24;
    public static final int LEGEND_ROW = 23;

    private static final String SYSTEM_NAME = "WMSPROD";
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");

    /**
     * Header band. The program id is the same name the operator would see in a job log,
     * which is deliberate — it is how support asks "what screen were you on".
     */
    public void drawHeader(TerminalScreen screen, String title, LocalDateTime now) {
        ScreenBuffer buffer = screen.getBuffer();
        buffer.write(1, 2, screen.getScreenId(), ScreenColor.BLUE);
        buffer.writeCentered(1, title, ScreenColor.WHITE);
        buffer.writeRight(1, 79, SYSTEM_NAME, ScreenColor.BLUE);
        buffer.writeRight(2, 79, now.format(STAMP), ScreenColor.BLUE);
    }

    /**
     * Function-key legend. Entries are spaced apart rather than padded to a fixed width,
     * matching how the legend reads on the stock IBM panels.
     */
    public void drawLegend(TerminalScreen screen, String legend) {
        screen.getBuffer().write(LEGEND_ROW, 2, legend, ScreenColor.BLUE);
    }

    /**
     * Message line. Errors are red; informational messages are pink, which is the
     * convention that keeps a warning visually distinct from a failure.
     */
    public void drawMessage(TerminalScreen screen) {
        String message = screen.getMessage();
        if (message == null || message.isEmpty()) {
            return;
        }
        ScreenColor color = screen.isMessageIsError() ? ScreenColor.RED : ScreenColor.PINK;
        screen.getBuffer().write(MESSAGE_ROW, 2, truncate(message, 78), color);
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }
}
