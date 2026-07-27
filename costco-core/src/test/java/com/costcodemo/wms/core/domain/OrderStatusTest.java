package com.costcodemo.wms.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class OrderStatusTest {

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void mapsEveryDeclaredLegacyCodeBackToItsStatus(OrderStatus status) {
        assertEquals(status, OrderStatus.fromLegacyCode(status.getLegacyCode()));
    }

    @Test
    void mapsTheKnownCodesToTheExpectedStatuses() {
        assertEquals(OrderStatus.OPEN, OrderStatus.fromLegacyCode("OP"));
        assertEquals(OrderStatus.SHIPPED, OrderStatus.fromLegacyCode("SH"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.fromLegacyCode("CN"));
    }

    @Test
    void acceptsLowerCaseCodes() {
        assertEquals(OrderStatus.SHIPPED, OrderStatus.fromLegacyCode("sh"));
    }

    @Test
    void acceptsSpacePaddedCodesFromFixedWidthFields() {
        assertEquals(OrderStatus.BACKORDER, OrderStatus.fromLegacyCode("  bo  "));
        assertEquals(OrderStatus.PICKING, OrderStatus.fromLegacyCode("PK "));
    }

    @Test
    void rejectsAnUnknownCodeAndNamesTheOffendingValue() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OrderStatus.fromLegacyCode("XX"));
        assertEquals("Unknown status code: XX", ex.getMessage());
    }

    @Test
    void rejectsABlankCodeAsUnknown() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OrderStatus.fromLegacyCode("   "));
        assertTrue(ex.getMessage().startsWith("Unknown status code:"), ex.getMessage());
    }

    @Test
    void rejectsANullCodeWithAClearMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> OrderStatus.fromLegacyCode(null));
        assertEquals("Status code is required", ex.getMessage());
    }
}
