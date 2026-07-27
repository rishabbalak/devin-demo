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
        return new InventoryBalance("W001", "0000123", "A-01-02", onHand, allocated, statusCode);
    }

    @Test
    void reportsOnHandMinusAllocatedWhenStockRemains() {
        assertEquals(60, balance(100, 40, "AV").getQuantityAvailable());
    }

    @ParameterizedTest
    @CsvSource({
            "10, 25",
            "0, 5",
            "-5, 0",
            "-5, 10"
    })
    void neverReportsANegativeAvailableQuantity(int onHand, int allocated) {
        assertEquals(0, balance(onHand, allocated, "AV").getQuantityAvailable());
    }

    @Test
    void reportsZeroWhenEverythingOnHandIsAllocated() {
        assertEquals(0, balance(42, 42, "AV").getQuantityAvailable());
    }

    @ParameterizedTest
    @ValueSource(strings = {"QC", "DM"})
    void treatsQualityHoldAndDamagedStockAsNonNettable(String statusCode) {
        assertFalse(balance(500, 0, statusCode).isNettable());
    }

    @Test
    void treatsAvailableStockAsNettable() {
        assertTrue(balance(500, 0, "AV").isNettable());
    }

    @Test
    void keepsTheOnHandQuantityOfNonNettableStockVisible() {
        InventoryBalance held = balance(500, 100, "QC");

        assertEquals(500, held.getQuantityOnHand());
        assertEquals(400, held.getQuantityAvailable());
        assertFalse(held.isNettable());
    }
}
