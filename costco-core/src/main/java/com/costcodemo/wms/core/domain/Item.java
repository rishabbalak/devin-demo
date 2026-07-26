package com.costcodemo.wms.core.domain;

import java.math.BigDecimal;

/**
 * A catalog item, translated out of the ITEMMAST physical file.
 *
 * <p>Costco carries fewer than 4,000 active SKUs per warehouse, so the item master is
 * small by broadline-retail standards — the seed data reflects that deliberately.
 */
public class Item {

    private final String itemNumber;
    private final String description;
    private final String department;
    private final String unitOfMeasure;
    private final BigDecimal price;
    private final boolean active;

    public Item(String itemNumber, String description, String department,
                String unitOfMeasure, BigDecimal price, boolean active) {
        this.itemNumber = itemNumber;
        this.description = description;
        this.department = department;
        this.unitOfMeasure = unitOfMeasure;
        this.price = price;
        this.active = active;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public String getDescription() {
        return description;
    }

    public String getDepartment() {
        return department;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public BigDecimal extendedPrice(int quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
