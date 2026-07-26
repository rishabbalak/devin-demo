package com.costcodemo.wms.terminal.screen.handler;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.costcodemo.wms.terminal.screen.ScreenChrome;
import com.costcodemo.wms.terminal.screen.ScreenColor;
import com.costcodemo.wms.terminal.screen.ScreenField;
import com.costcodemo.wms.terminal.screen.ScreenHandler;
import com.costcodemo.wms.terminal.screen.TerminalScreen;
import com.costcodemo.wms.terminal.session.TerminalInput;
import com.costcodemo.wms.terminal.session.TerminalSession;

/**
 * The sign-on panel.
 *
 * <p>This is a demonstration prop and performs no authentication whatsoever. Any user id is
 * accepted and the password field is ignored entirely — it exists because a 5250 session
 * without one would not read as a 5250 session. Nothing is stored, checked or transmitted.
 */
@Component
public class SignOnHandler implements ScreenHandler {

    private final ScreenChrome chrome;

    public SignOnHandler(ScreenChrome chrome) {
        this.chrome = chrome;
    }

    @Override
    public String screenId() {
        return TerminalSession.SIGN_ON;
    }

    @Override
    public TerminalScreen render(TerminalSession session) {
        TerminalScreen screen = new TerminalScreen(screenId());
        chrome.drawHeader(screen, "Sign On", LocalDateTime.now());

        screen.getBuffer().write(6, 40, "System  . . . . . :", ScreenColor.GREEN);
        screen.getBuffer().write(6, 61, "WMSPROD", ScreenColor.GREEN);
        screen.getBuffer().write(7, 40, "Subsystem . . . . :", ScreenColor.GREEN);
        screen.getBuffer().write(7, 61, "QINTER", ScreenColor.GREEN);
        screen.getBuffer().write(8, 40, "Display . . . . . :", ScreenColor.GREEN);
        screen.getBuffer().write(8, 61, "QPADEV0007", ScreenColor.GREEN);

        screen.getBuffer().write(12, 7, "User  . . . . . . . . . . . . .", ScreenColor.GREEN);
        screen.addField(new ScreenField("USERID", 12, 41, 10, "", ScreenColor.WHITE, false));

        screen.getBuffer().write(13, 7, "Password  . . . . . . . . . . .", ScreenColor.GREEN);
        screen.addField(new ScreenField("PASSWD", 13, 41, 10, "", ScreenColor.WHITE, false));

        screen.getBuffer().write(15, 7, "Program/procedure . . . . . . .", ScreenColor.GREEN);
        screen.getBuffer().write(16, 7, "Menu  . . . . . . . . . . . . .", ScreenColor.GREEN);
        screen.getBuffer().write(17, 7, "Current library . . . . . . . .", ScreenColor.GREEN);

        screen.setCursorField("USERID");
        chrome.drawLegend(screen, "F3=Exit   F12=Cancel");

        String pending = session.consumePendingMessage();
        if (!pending.isEmpty()) {
            if (session.isPendingMessageError()) {
                screen.setError(pending, "USERID");
            } else {
                screen.setInfo(pending);
            }
        }
        chrome.drawMessage(screen);
        return screen;
    }

    @Override
    public void handle(TerminalSession session, TerminalInput input) {
        String userId = input.get("USERID");
        if (userId.isEmpty()) {
            session.setPendingMessage("User is required.", true);
            return;
        }
        session.setUserId(userId);
        session.setCurrentScreen(TerminalSession.MAIN_MENU);
    }
}
