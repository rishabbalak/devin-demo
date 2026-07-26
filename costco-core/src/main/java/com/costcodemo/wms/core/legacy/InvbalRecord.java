package com.costcodemo.wms.core.legacy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Inventory balance by warehouse, item and location. Physical file WMSDTA/INVBAL.
 *
 * <p>Keyed on the relative record number rather than a declared composite key. RRN is how
 * the RPG programs address this file, and the logical view over warehouse plus item is
 * what enforces uniqueness — the physical file itself declares none.
 */
@Entity
@Table(name = "INVBAL")
public class InvbalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IBRRN", nullable = false)
    private Long relativeRecordNumber;

    @Column(name = "IBWHS", length = 4, nullable = false)
    private String warehouseCode;

    @Column(name = "IBITEM", length = 10, nullable = false)
    private String itemNumber;

    @Column(name = "IBLOCN", length = 9, nullable = false)
    private String location;

    @Column(name = "IBQOH", nullable = false)
    private Integer quantityOnHand;

    @Column(name = "IBQALC", nullable = false)
    private Integer quantityAllocated;

    @Column(name = "IBSTAT", length = 2, nullable = false)
    private String statusCode;

    public Long getRelativeRecordNumber() {
        return relativeRecordNumber;
    }

    public void setRelativeRecordNumber(Long relativeRecordNumber) {
        this.relativeRecordNumber = relativeRecordNumber;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Integer quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Integer getQuantityAllocated() {
        return quantityAllocated;
    }

    public void setQuantityAllocated(Integer quantityAllocated) {
        this.quantityAllocated = quantityAllocated;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
