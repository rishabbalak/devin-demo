package com.costcodemo.wms.core.domain;

/**
 * On-hand and allocated quantities for one item in one location, translated out of the
 * INVBAL physical file.
 *
 * <p>Available is derived rather than stored. The legacy file keeps on-hand and allocated
 * as separate packed-decimal fields and every RPG program recomputes the difference, which
 * is why there is no IBQAVL column to read.
 */
public class InventoryBalance {

    private final String warehouseCode;
    private final String itemNumber;
    private final String location;
    private final int quantityOnHand;
    private final int quantityAllocated;
    private final String statusCode;

    public InventoryBalance(String warehouseCode, String itemNumber, String location,
                            int quantityOnHand, int quantityAllocated, String statusCode) {
        this.warehouseCode = warehouseCode;
        this.itemNumber = itemNumber;
        this.location = location;
        this.quantityOnHand = quantityOnHand;
        this.quantityAllocated = quantityAllocated;
        this.statusCode = statusCode;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public String getLocation() {
        return location;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public int getQuantityAllocated() {
        return quantityAllocated;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public int getQuantityAvailable() {
        int available = quantityOnHand - quantityAllocated;
        return Math.max(available, 0);
    }

    /**
     * Quality-hold and damaged stock sits in the file with a real on-hand quantity but must
     * never be promised to an order. The green screen shows these locations in red.
     */
    public boolean isNettable() {
        return !"QC".equals(statusCode) && !"DM".equals(statusCode);
    }
}
