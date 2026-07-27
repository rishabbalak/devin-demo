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
    void everyStatusRoundTripsThroughItsLegacyCode(OrderStatus status) {
        assertSame(status, OrderStatus.fromLegacyCode(status.getLegacyCode()));
    }

    @ParameterizedTest
    @CsvSource({"OP,OPEN", "AL,ALLOCATED", "RL,RELEASED", "PK,PICKING", "PP,PACKED",
            "SH,SHIPPED", "BO,BACKORDER", "HL,HELD", "CN,CANCELLED"})
    void resolvesTheStoredCodes(String code, OrderStatus expected) {
        assertSame(expected, OrderStatus.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"sh", "sH", "Sh"})
    void acceptsLowerCaseInput(String code) {
        assertSame(OrderStatus.SHIPPED, OrderStatus.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"OP ", " OP", " op ", "\tOP\n"})
    void acceptsSpacePaddedInput(String code) {
        assertSame(OrderStatus.OPEN, OrderStatus.fromLegacyCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ZZ", "", "   ", "OPEN", "O"})
    void unknownCodeFailsAndNamesTheOffendingCode(String code) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OrderStatus.fromLegacyCode(code));
        assertTrue(ex.getMessage().startsWith("Unknown status code: "),
                "unexpected message: " + ex.getMessage());
        assertTrue(ex.getMessage().endsWith(code),
                "message should echo the input verbatim but was: " + ex.getMessage());
    }

    @Test
    void nullCodeFailsAsAMissingRequiredValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OrderStatus.fromLegacyCode(null));
        assertEquals("Status code is required", ex.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"BACKORDER", "HELD", "CANCELLED"})
    void backorderHeldAndCancelledAreExceptionStates(OrderStatus status) {
        assertTrue(status.isException());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class,
            names = {"BACKORDER", "HELD", "CANCELLED"}, mode = EnumSource.Mode.EXCLUDE)
    void everyOtherStateIsNotAnException(OrderStatus status) {
        assertFalse(status.isException());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"SHIPPED", "CANCELLED"}, mode = EnumSource.Mode.EXCLUDE)
    void ordersStayOpenUntilShippedOrCancelled(OrderStatus status) {
        assertTrue(status.isOpen());
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"SHIPPED", "CANCELLED"})
    void shippedAndCancelledOrdersAreClosed(OrderStatus status) {
        assertFalse(status.isOpen());
    }

    @Test
    void carriesTheDisplayName() {
        assertEquals("Backorder", OrderStatus.BACKORDER.getDisplayName());
        assertEquals("BO", OrderStatus.BACKORDER.getLegacyCode());
    }
}
