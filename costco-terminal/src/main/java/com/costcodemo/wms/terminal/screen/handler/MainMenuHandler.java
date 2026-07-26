package com.costcodemo.wms.terminal.screen.handler;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.costcodemo.wms.terminal.screen.ScreenChrome;
import com.costcodemo.wms.terminal.screen.ScreenColor;
import com.costcodemo.wms.terminal.screen.ScreenField;
import com.costcodemo.wms.terminal.screen.ScreenHandler;
import com.costcodemo.wms.terminal.screen.TerminalScreen;
import com.costcodemo.wms.terminal.session.AidKey;
import com.costcodemo.wms.terminal.session.TerminalInput;
import com.costcodemo.wms.terminal.session.TerminalSession;

/**
 * The application menu.
 *
 * <p>Menu options are numbered rather than selected with a cursor, and the entry field
 * doubles as a command line. Options that are not implemented in this reference build still
 * appear on the menu, because a real menu on a system this age always has entries that lead
 * to programs nobody has run in years.
 */
@Component
public class MainMenuHandler implements ScreenHandler {

    private final ScreenChrome chrome;

    public MainMenuHandler(ScreenChrome chrome) {
        this.chrome = chrome;
    }

    @Override
    public String screenId() {
        return TerminalSession.MAIN_MENU;
    }

    @Override
    public TerminalScreen render(TerminalSession session) {
        TerminalScreen screen = new TerminalScreen(screenId());
        chrome.drawHeader(screen, "Warehouse Management System", LocalDateTime.now());

        screen.getBuffer().write(4, 2, "Select one of the following:", ScreenColor.TURQUOISE);

        option(screen, 6, " 1.", "Member inquiry and renewal");
        option(screen, 7, " 2.", "Inventory balance inquiry");
        option(screen, 8, " 3.", "Order inquiry and maintenance");
        option(screen, 9, " 4.", "Inbound receiving and ASN control");
        option(screen, 10, " 5.", "Putaway and replenishment");
        option(screen, 11, " 6.", "Wave planning and release");
        option(screen, 12, " 7.", "Picking and packing");
        option(screen, 13, " 8.", "Shipping, manifest and BOL");
        option(screen, 14, " 9.", "Cycle counting and audit");
        option(screen, 15, "10.", "Cross-dock control");
        option(screen, 16, "11.", "Reports and inquiries");
        option(screen, 18, "90.", "Sign off");

        screen.getBuffer().write(20, 2, "Selection or command", ScreenColor.TURQUOISE);
        screen.getBuffer().write(21, 2, "===>", ScreenColor.TURQUOISE);
        screen.addField(new ScreenField("OPTION", 21, 7, 60, "", ScreenColor.WHITE, false));
        screen.setCursorField("OPTION");

        chrome.drawLegend(screen, "F3=Exit   F4=Prompt   F9=Retrieve   F12=Cancel");

        String pending = session.consumePendingMessage();
        if (!pending.isEmpty()) {
            if (session.isPendingMessageError()) {
                screen.setError(pending, "OPTION");
            } else {
                screen.setInfo(pending);
            }
        }
        chrome.drawMessage(screen);
        return screen;
    }

    private void option(TerminalScreen screen, int row, String number, String text) {
        screen.getBuffer().write(row, 6, number, ScreenColor.WHITE);
        screen.getBuffer().write(row, 11, text, ScreenColor.GREEN);
    }

    @Override
    public void handle(TerminalSession session, TerminalInput input) {
        if (input.getAidKey() == AidKey.F3) {
            session.signOff();
            return;
        }

        String option = input.get("OPTION");
        if (option.isEmpty()) {
            return;
        }

        switch (option) {
            case "1":
                session.pushScreen("WMS110");
                return;
            case "2":
                session.pushScreen("WMS210");
                return;
            case "3":
                session.pushScreen("WMS310");
                return;
            case "90":
                session.signOff();
                return;
            default:
                break;
        }

        if (isImplementedElsewhere(option)) {
            session.setPendingMessage(
                    "Option " + option + " is not available in this reference build.", true);
        } else {
            session.setPendingMessage(
                    "Option " + option + " is not valid.  Type a valid option from the list above.",
                    true);
        }
    }

    /** Options that exist on the menu but whose programs are not part of this build. */
    private boolean isImplementedElsewhere(String option) {
        switch (option) {
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
            case "10":
            case "11":
                return true;
            default:
                return false;
        }
    }
}
