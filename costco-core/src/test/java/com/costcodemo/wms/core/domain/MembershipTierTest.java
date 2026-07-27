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
    void everyTierRoundTripsThroughItsLegacyCode(MembershipTier tier) {
        assertSame(tier, MembershipTier.fromLegacyCode(tier.getLegacyCode()));
    }

    @ParameterizedTest
    @CsvSource({"GS,GOLD_STAR", "BU,BUSINESS", "EX,EXECUTIVE"})
    void resolvesTheStoredCodes(String code, MembershipTier expected) {
        assertSame(expected, MembershipTier.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ex", "eX", "Ex"})
    void acceptsLowerCaseInput(String code) {
        assertSame(MembershipTier.EXECUTIVE, MembershipTier.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GS ", " GS", "  GS  ", "\tgs\n"})
    void acceptsSpacePaddedInput(String code) {
        assertSame(MembershipTier.GOLD_STAR, MembershipTier.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ZZ", "", "   ", "GOLD_STAR", "G"})
    void unknownCodeFailsAndNamesTheOffendingCode(String code) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MembershipTier.fromLegacyCode(code));
        assertTrue(ex.getMessage().startsWith("Unknown tier code: "),
                "unexpected message: " + ex.getMessage());
        assertTrue(ex.getMessage().endsWith(code),
                "message should echo the input verbatim but was: " + ex.getMessage());
    }

    @Test
    void nullCodeFailsAsAMissingRequiredValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> MembershipTier.fromLegacyCode(null));
        assertEquals("Tier code is required", ex.getMessage());
    }

    @Test
    void onlyExecutiveEarnsTheAnnualReward() {
        assertTrue(MembershipTier.EXECUTIVE.earnsAnnualReward());
        assertEquals(false, MembershipTier.GOLD_STAR.earnsAnnualReward());
        assertEquals(false, MembershipTier.BUSINESS.earnsAnnualReward());
    }

    @Test
    void carriesTheDisplayNameAndFee() {
        assertEquals("Executive", MembershipTier.EXECUTIVE.getDisplayName());
        assertEquals(130, MembershipTier.EXECUTIVE.getAnnualFeeUsd());
        assertEquals("Gold Star", MembershipTier.GOLD_STAR.getDisplayName());
        assertEquals(65, MembershipTier.GOLD_STAR.getAnnualFeeUsd());
    }
}
