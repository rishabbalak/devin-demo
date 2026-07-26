package com.costcodemo.wms.core.service;

/**
 * One requested line on a new order, before it has been priced or allocated.
 */
public class OrderLineRequest {

    private final String itemNumber;
    private final int quantity;

    public OrderLineRequest(String itemNumber, int quantity) {
        this.itemNumber = itemNumber;
        this.quantity = quantity;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public int getQuantity() {
        return quantity;
    }
}
