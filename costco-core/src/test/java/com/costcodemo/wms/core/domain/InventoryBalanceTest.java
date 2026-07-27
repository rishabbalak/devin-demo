package com.costcodemo.wms.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class InventoryBalanceTest {

    private static InventoryBalance balance(int onHand, int allocated, String statusCode) {
        return new InventoryBalance("W001", "0000123", "A-01-01", onHand, allocated, statusCode);
    }

    @Test
    void exposesTheTranslatedRecordFields() {
        InventoryBalance stock = balance(40, 15, "AV");

        assertEquals("W001", stock.getWarehouseCode());
        assertEquals("0000123", stock.getItemNumber());
        assertEquals("A-01-01", stock.getLocation());
        assertEquals(40, stock.getQuantityOnHand());
        assertEquals(15, stock.getQuantityAllocated());
        assertEquals("AV", stock.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({
            "40, 15, 25",
            "40, 40, 0",
            "0, 0, 0",
            "10, 25, 0",
            "0, 500, 0",
            "-5, 0, 0"
    })
    void neverReportsANegativeAvailableQuantity(int onHand, int allocated, int expectedAvailable) {
        InventoryBalance stock = balance(onHand, allocated, "AV");

        assertEquals(expectedAvailable, stock.getQuantityAvailable());
        assertTrue(stock.getQuantityAvailable() >= 0);
    }

    @Test
    void overAllocatedStockStillReportsItsRawOnHandAndAllocated() {
        InventoryBalance stock = balance(10, 25, "AV");

        assertEquals(0, stock.getQuantityAvailable());
        assertEquals(10, stock.getQuantityOnHand());
        assertEquals(25, stock.getQuantityAllocated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"QC", "DM"})
    void qualityHoldAndDamagedStockIsNotNettable(String statusCode) {
        InventoryBalance stock = balance(100, 0, statusCode);

        assertFalse(stock.isNettable());
        assertEquals(100, stock.getQuantityAvailable());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AV", "OK", "HD", ""})
    void everyOtherStatusCodeIsNettable(String statusCode) {
        assertTrue(balance(100, 0, statusCode).isNettable());
    }

    @Test
    void aMissingStatusCodeIsNettable() {
        assertTrue(balance(100, 0, null).isNettable());
    }
}
