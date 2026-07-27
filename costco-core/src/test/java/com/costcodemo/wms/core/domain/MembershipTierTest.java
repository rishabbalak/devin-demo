package com.costcodemo.wms.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class MembershipTierTest {

    @ParameterizedTest
    @EnumSource(MembershipTier.class)
    void resolvesEveryTierFromItsOwnLegacyCode(MembershipTier tier) {
        assertSame(tier, MembershipTier.fromLegacyCode(tier.getLegacyCode()));
    }

    @ParameterizedTest
    @CsvSource({
            "GS, GOLD_STAR",
            "BU, BUSINESS",
            "EX, EXECUTIVE"
    })
    void mapsTheStoredMbtierCodes(String code, MembershipTier expected) {
        assertSame(expected, MembershipTier.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ex", "eX", "  EX", "EX  ", "  ex  ", "\tEX\n"})
    void acceptsLowerCaseAndSpacePaddedCodesFromFixedWidthFields(String code) {
        assertSame(MembershipTier.EXECUTIVE, MembershipTier.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ZZ", "", "   ", "G", "GOLD_STAR", "gs1"})
    void rejectsUnknownCodesWithAMessageNamingTheCode(String code) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MembershipTier.fromLegacyCode(code));

        assertTrue(ex.getMessage().startsWith("Unknown tier code: "), ex.getMessage());
        assertTrue(ex.getMessage().contains(code), ex.getMessage());
    }

    @Test
    void rejectsANullCodeWithADistinctMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MembershipTier.fromLegacyCode(null));

        assertEquals("Tier code is required", ex.getMessage());
    }

    @Test
    void carriesTheFeeAndDisplayNameForEachTier() {
        assertEquals("Gold Star", MembershipTier.GOLD_STAR.getDisplayName());
        assertEquals(65, MembershipTier.GOLD_STAR.getAnnualFeeUsd());
        assertEquals(65, MembershipTier.BUSINESS.getAnnualFeeUsd());
        assertEquals(130, MembershipTier.EXECUTIVE.getAnnualFeeUsd());
    }

    @Test
    void onlyExecutiveEarnsTheAnnualReward() {
        assertTrue(MembershipTier.EXECUTIVE.earnsAnnualReward());
        assertEquals(false, MembershipTier.GOLD_STAR.earnsAnnualReward());
        assertEquals(false, MembershipTier.BUSINESS.earnsAnnualReward());
    }
}
