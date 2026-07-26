package com.costcodemo.wms.core.domain;

/**
 * Membership tiers. The core stores these as two-character codes in MBRMAST.MBTIER,
 * which is why the legacy code is carried on the enum rather than derived from the name.
 */
public enum MembershipTier {

    GOLD_STAR("GS", "Gold Star", 65),
    BUSINESS("BU", "Business", 65),
    EXECUTIVE("EX", "Executive", 130);

    private final String legacyCode;
    private final String displayName;
    private final int annualFeeUsd;

    MembershipTier(String legacyCode, String displayName, int annualFeeUsd) {
        this.legacyCode = legacyCode;
        this.displayName = displayName;
        this.annualFeeUsd = annualFeeUsd;
    }

    public String getLegacyCode() {
        return legacyCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAnnualFeeUsd() {
        return annualFeeUsd;
    }

    /** Executive members earn a 2% annual reward, capped by Costco at $1,250. */
    public boolean earnsAnnualReward() {
        return this == EXECUTIVE;
    }

    public static MembershipTier fromLegacyCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Tier code is required");
        }
        String trimmed = code.trim().toUpperCase();
        for (MembershipTier tier : values()) {
            if (tier.legacyCode.equals(trimmed)) {
                return tier;
            }
        }
        throw new IllegalArgumentException("Unknown tier code: " + code);
    }
}
