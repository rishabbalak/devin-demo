package com.costcodemo.wms.core.domain;

/**
 * Order lifecycle states. Stored in ORDHDR.OHSTAT as two-character codes.
 */
public enum OrderStatus {

    OPEN("OP", "Open"),
    ALLOCATED("AL", "Allocated"),
    RELEASED("RL", "Released"),
    PICKING("PK", "Picking"),
    PACKED("PP", "Packed"),
    SHIPPED("SH", "Shipped"),
    BACKORDER("BO", "Backorder"),
    HELD("HL", "Held"),
    CANCELLED("CN", "Cancelled");

    private final String legacyCode;
    private final String displayName;

    OrderStatus(String legacyCode, String displayName) {
        this.legacyCode = legacyCode;
        this.displayName = displayName;
    }

    public String getLegacyCode() {
        return legacyCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Exception states render in red on the terminal; shipped renders green. */
    public boolean isException() {
        return this == BACKORDER || this == HELD || this == CANCELLED;
    }

    public boolean isOpen() {
        return this != SHIPPED && this != CANCELLED;
    }

    public static OrderStatus fromLegacyCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Status code is required");
        }
        String trimmed = code.trim().toUpperCase();
        for (OrderStatus status : values()) {
            if (status.legacyCode.equals(trimmed)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status code: " + code);
    }
}
