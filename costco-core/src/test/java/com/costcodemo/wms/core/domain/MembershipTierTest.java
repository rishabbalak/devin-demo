package com.costcodemo.wms.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MembershipTierTest {

    @ParameterizedTest
    @EnumSource(MembershipTier.class)
    void mapsEveryDeclaredLegacyCodeBackToItsTier(MembershipTier tier) {
        assertEquals(tier, MembershipTier.fromLegacyCode(tier.getLegacyCode()));
    }

    @Test
    void mapsTheKnownCodesToTheExpectedTiers() {
        assertEquals(MembershipTier.GOLD_STAR, MembershipTier.fromLegacyCode("GS"));
        assertEquals(MembershipTier.BUSINESS, MembershipTier.fromLegacyCode("BU"));
        assertEquals(MembershipTier.EXECUTIVE, MembershipTier.fromLegacyCode("EX"));
    }

    @Test
    void acceptsLowerCaseCodes() {
        assertEquals(MembershipTier.EXECUTIVE, MembershipTier.fromLegacyCode("ex"));
    }

    @Test
    void acceptsSpacePaddedCodesFromFixedWidthFields() {
        assertEquals(MembershipTier.GOLD_STAR, MembershipTier.fromLegacyCode("  gs  "));
        assertEquals(MembershipTier.BUSINESS, MembershipTier.fromLegacyCode("BU "));
    }

    @Test
    void rejectsAnUnknownCodeAndNamesTheOffendingValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MembershipTier.fromLegacyCode("ZZ"));
        assertEquals("Unknown tier code: ZZ", ex.getMessage());
    }

    @Test
    void rejectsABlankCodeAsUnknown() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MembershipTier.fromLegacyCode("   "));
        assertTrue(ex.getMessage().startsWith("Unknown tier code:"), ex.getMessage());
    }

    @Test
    void rejectsANullCodeWithAClearMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MembershipTier.fromLegacyCode(null));
        assertEquals("Tier code is required", ex.getMessage());
    }
}
