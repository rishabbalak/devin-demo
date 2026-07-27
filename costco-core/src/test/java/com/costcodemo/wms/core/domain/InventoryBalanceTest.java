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
        return new InventoryBalance("W01", "0000123", "A-01-02", onHand, allocated, statusCode);
    }

    @Test
    void availableIsOnHandMinusAllocated() {
        InventoryBalance balance = balance(100, 40, "AV");
        assertEquals(60, balance.getQuantityAvailable());
        assertEquals(100, balance.getQuantityOnHand());
        assertEquals(40, balance.getQuantityAllocated());
    }

    @ParameterizedTest
    @CsvSource({"0,10", "5,7", "40,100", "-3,0", "-3,5"})
    void availableIsNeverNegative(int onHand, int allocated) {
        assertEquals(0, balance(onHand, allocated, "AV").getQuantityAvailable());
    }

    @Test
    void exhaustedStockReportsZeroRatherThanTheOverAllocation() {
        InventoryBalance overAllocated = balance(12, 30, "AV");
        assertEquals(0, overAllocated.getQuantityAvailable());
        // The underlying legacy fields are left untouched; only the derived value is floored.
        assertEquals(12, overAllocated.getQuantityOnHand());
        assertEquals(30, overAllocated.getQuantityAllocated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"QC", "DM"})
    void quarantineAndDamagedStockIsNotNettable(String statusCode) {
        InventoryBalance held = balance(500, 0, statusCode);
        assertFalse(held.isNettable(),
                "status " + statusCode + " must never be promised to an order");
        // Held stock still carries a real on-hand quantity in the file.
        assertEquals(500, held.getQuantityOnHand());
        assertEquals(500, held.getQuantityAvailable());
        assertEquals(statusCode, held.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AV", "OK", "", "qc", "dm", "QCX"})
    void anyOtherStatusIsNettable(String statusCode) {
        assertTrue(balance(10, 0, statusCode).isNettable());
    }

    @Test
    void missingStatusIsNettable() {
        assertTrue(balance(10, 0, null).isNettable());
    }

    @Test
    void carriesTheLocationKeyFromTheLegacyRecord() {
        InventoryBalance balance = balance(1, 0, "AV");
        assertEquals("W01", balance.getWarehouseCode());
        assertEquals("0000123", balance.getItemNumber());
        assertEquals("A-01-02", balance.getLocation());
    }
}
