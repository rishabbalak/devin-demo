package com.costcodemo.wms.core.legacy;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Order detail line. Physical file WMSDTA/ORDDTL.
 *
 * <p>Joined to ORDHDR on order number in application code. There is no declared foreign
 * key — the file was designed for record-level access from RPG, which enforced the
 * relationship in the program rather than the database.
 */
@Entity
@Table(name = "ORDDTL")
public class OrddtlRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ODRRN", nullable = false)
    private Long relativeRecordNumber;

    @Column(name = "ODORDN", length = 10, nullable = false)
    private String orderNumber;

    @Column(name = "ODLINE", nullable = false)
    private Integer lineNumber;

    @Column(name = "ODITEM", length = 10, nullable = false)
    private String itemNumber;

    @Column(name = "ODQORD", nullable = false)
    private Integer quantityOrdered;

    @Column(name = "ODQALC", nullable = false)
    private Integer quantityAllocated;

    @Column(name = "ODUOM", length = 2, nullable = false)
    private String unitOfMeasure;

    @Column(name = "ODPRIC", precision = 11, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    public Long getRelativeRecordNumber() {
        return relativeRecordNumber;
    }

    public void setRelativeRecordNumber(Long relativeRecordNumber) {
        this.relativeRecordNumber = relativeRecordNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public Integer getQuantityOrdered() {
        return quantityOrdered;
    }

    public void setQuantityOrdered(Integer quantityOrdered) {
        this.quantityOrdered = quantityOrdered;
    }

    public Integer getQuantityAllocated() {
        return quantityAllocated;
    }

    public void setQuantityAllocated(Integer quantityAllocated) {
        this.quantityAllocated = quantityAllocated;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
