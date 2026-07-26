package com.costcodemo.wms.terminal.screen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.costcodemo.wms.terminal.session.TerminalSession;

/**
 * Routes a screen id to the handler that owns it.
 *
 * <p>Handlers register themselves by being Spring beans, which is the modern equivalent of
 * the program-call table this application would otherwise carry.
 */
@Component
public class ScreenRegistry {

    private final Map<String, ScreenHandler> handlers = new HashMap<>();

    public ScreenRegistry(List<ScreenHandler> discovered) {
        for (ScreenHandler handler : discovered) {
            handlers.put(handler.screenId(), handler);
        }
    }

    /**
     * The handler for a screen id, falling back to the sign-on panel. An unknown screen id
     * means the session is in a state no program can service, and dropping the operator back
     * to sign-on is what the subsystem would do.
     */
    public ScreenHandler resolve(String screenId) {
        ScreenHandler handler = handlers.get(screenId);
        return handler != null ? handler : handlers.get(TerminalSession.SIGN_ON);
    }

    public boolean isKnown(String screenId) {
        return handlers.containsKey(screenId);
    }
}
