package com.costcodemo.wms.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class OrderStatusTest {

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void resolvesEveryStatusFromItsOwnLegacyCode(OrderStatus status) {
        assertSame(status, OrderStatus.fromLegacyCode(status.getLegacyCode()));
    }

    @ParameterizedTest
    @CsvSource({
            "OP, OPEN",
            "AL, ALLOCATED",
            "RL, RELEASED",
            "PK, PICKING",
            "PP, PACKED",
            "SH, SHIPPED",
            "BO, BACKORDER",
            "HL, HELD",
            "CN, CANCELLED"
    })
    void mapsTheStoredOhstatCodes(String code, OrderStatus expected) {
        assertSame(expected, OrderStatus.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sh", "sH", " SH", "SH ", "  sh  "})
    void acceptsLowerCaseAndSpacePaddedCodesFromFixedWidthFields(String code) {
        assertSame(OrderStatus.SHIPPED, OrderStatus.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ZZ", "", "   ", "O", "OPEN", "op1"})
    void rejectsUnknownCodesWithAMessageNamingTheCode(String code) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OrderStatus.fromLegacyCode(code));

        assertTrue(ex.getMessage().startsWith("Unknown status code: "), ex.getMessage());
        assertTrue(ex.getMessage().contains(code), ex.getMessage());
    }

    @Test
    void rejectsANullCodeWithADistinctMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OrderStatus.fromLegacyCode(null));

        assertEquals("Status code is required", ex.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"BACKORDER", "HELD", "CANCELLED"})
    void flagsExceptionStates(OrderStatus status) {
        assertTrue(status.isException());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class,
            names = {"BACKORDER", "HELD", "CANCELLED"},
            mode = EnumSource.Mode.EXCLUDE)
    void doesNotFlagNormalStatesAsExceptions(OrderStatus status) {
        assertFalse(status.isException());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"SHIPPED", "CANCELLED"})
    void terminalStatesAreNotOpen(OrderStatus status) {
        assertFalse(status.isOpen());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class,
            names = {"SHIPPED", "CANCELLED"},
            mode = EnumSource.Mode.EXCLUDE)
    void everyOtherStateIsStillOpen(OrderStatus status) {
        assertTrue(status.isOpen());
        assertEquals(2, status.getLegacyCode().length());
    }
}
