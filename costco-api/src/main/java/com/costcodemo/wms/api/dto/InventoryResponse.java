package com.costcodemo.wms.api.dto;

/**
 * A single inventory balance row.
 *
 * <p>{@code available} is derived, and is zero on any row that is not nettable regardless of
 * what is physically on hand — quality-hold and damaged stock cannot be promised to an order.
 */
public record InventoryResponse(
        String warehouseCode,
        String itemNumber,
        String location,
        int quantityOnHand,
        int quantityAllocated,
        int quantityAvailable,
        String status,
        boolean nettable) {
}
