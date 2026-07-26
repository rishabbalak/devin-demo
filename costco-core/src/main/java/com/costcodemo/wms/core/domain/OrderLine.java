package com.costcodemo.wms.core.domain;

import java.math.BigDecimal;

/**
 * One line of an order, translated out of the ORDDTL physical file.
 */
public class OrderLine {

    private final int lineNumber;
    private final String itemNumber;
    private final String description;
    private final int quantityOrdered;
    private final int quantityAllocated;
    private final String unitOfMeasure;
    private final BigDecimal unitPrice;

    public OrderLine(int lineNumber, String itemNumber, String description, int quantityOrdered,
                     int quantityAllocated, String unitOfMeasure, BigDecimal unitPrice) {
        this.lineNumber = lineNumber;
        this.itemNumber = itemNumber;
        this.description = description;
        this.quantityOrdered = quantityOrdered;
        this.quantityAllocated = quantityAllocated;
        this.unitOfMeasure = unitOfMeasure;
        this.unitPrice = unitPrice;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantityOrdered() {
        return quantityOrdered;
    }

    public int getQuantityAllocated() {
        return quantityAllocated;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantityBackordered() {
        return Math.max(quantityOrdered - quantityAllocated, 0);
    }

    public boolean isBackordered() {
        return getQuantityBackordered() > 0;
    }

    public BigDecimal getExtendedAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantityOrdered));
    }
}
