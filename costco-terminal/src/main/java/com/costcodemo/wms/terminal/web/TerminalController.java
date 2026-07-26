package com.costcodemo.wms.terminal.web;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.costcodemo.wms.terminal.screen.ScreenHandler;
import com.costcodemo.wms.terminal.screen.ScreenRegistry;
import com.costcodemo.wms.terminal.screen.ScreenRenderer;
import com.costcodemo.wms.terminal.screen.TerminalScreen;
import com.costcodemo.wms.terminal.session.AidKey;
import com.costcodemo.wms.terminal.session.TerminalInput;
import com.costcodemo.wms.terminal.session.TerminalSession;

/**
 * The workstation controller.
 *
 * <p>There are exactly two operations, which is the whole interaction model of a 5250: read
 * the current screen, and submit the entire screen back. Nothing happens field by field.
 *
 * <p>Submits redirect before rendering so that a browser reload repaints the current screen
 * rather than re-sending the last one — the terminal equivalent of pressing an AID key twice
 * by accident.
 */
@Controller
public class TerminalController {

    private static final String SESSION_KEY = "wms.terminal.session";

    private final ScreenRegistry registry;
    private final ScreenRenderer renderer;

    public TerminalController(ScreenRegistry registry, ScreenRenderer renderer) {
        this.registry = registry;
        this.renderer = renderer;
    }

    @GetMapping("/wms")
    public String display(HttpSession httpSession, Model model) {
        TerminalSession session = sessionFor(httpSession);
        ScreenHandler handler = registry.resolve(session.getCurrentScreen());
        TerminalScreen screen = handler.render(session);

        model.addAttribute("screenHtml", renderer.toHtml(screen.getBuffer()));
        model.addAttribute("fields", screen.getFields());
        model.addAttribute("screenId", screen.getScreenId());
        model.addAttribute("cursorField", screen.getCursorField() == null ? "" : screen.getCursorField());
        model.addAttribute("alarm", screen.isMessageIsError());
        return "terminal";
    }

    @PostMapping("/wms")
    public String submit(@RequestParam Map<String, String> parameters,
                         @RequestParam(name = "__aid", required = false) String aid,
                         HttpSession httpSession) {

        TerminalSession session = sessionFor(httpSession);

        Map<String, String> fields = new HashMap<>(parameters);
        fields.remove("__aid");

        TerminalInput input = new TerminalInput(AidKey.parse(aid), fields);
        AidKey key = input.getAidKey();

        if (key == AidKey.F3) {
            handleExit(session, input);
            return "redirect:/wms";
        }
        if (key == AidKey.F12) {
            session.popScreen();
            return "redirect:/wms";
        }

        registry.resolve(session.getCurrentScreen()).handle(session, input);
        return "redirect:/wms";
    }

    /**
     * F3 leaves the current program. On the menu itself there is nothing left to leave, so
     * it signs the operator off; the sign-on panel ignores it entirely.
     */
    private void handleExit(TerminalSession session, TerminalInput input) {
        String current = session.getCurrentScreen();
        if (TerminalSession.SIGN_ON.equals(current)) {
            return;
        }
        if (TerminalSession.MAIN_MENU.equals(current)) {
            registry.resolve(current).handle(session, input);
            return;
        }
        session.exitToMenu();
    }

    private TerminalSession sessionFor(HttpSession httpSession) {
        Object existing = httpSession.getAttribute(SESSION_KEY);
        if (existing instanceof TerminalSession) {
            return (TerminalSession) existing;
        }
        TerminalSession session = new TerminalSession();
        httpSession.setAttribute(SESSION_KEY, session);
        return session;
    }
}
