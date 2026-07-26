package com.costcodemo.wms.terminal.session;

import java.util.Collections;
import java.util.Map;

/**
 * One whole-screen submit: the AID key that caused it plus every input field's contents.
 */
public class TerminalInput {

    private final AidKey aidKey;
    private final Map<String, String> fields;

    public TerminalInput(AidKey aidKey, Map<String, String> fields) {
        this.aidKey = aidKey == null ? AidKey.ENTER : aidKey;
        this.fields = fields == null ? Collections.emptyMap() : fields;
    }

    public AidKey getAidKey() {
        return aidKey;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    /** Field contents, trimmed and folded to upper case as a 5250 input field would be. */
    public String get(String name) {
        String value = fields.get(name);
        return value == null ? "" : value.trim().toUpperCase();
    }

    public String getRaw(String name) {
        String value = fields.get(name);
        return value == null ? "" : value.trim();
    }
}
