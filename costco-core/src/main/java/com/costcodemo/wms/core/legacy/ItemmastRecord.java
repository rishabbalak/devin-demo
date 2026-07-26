package com.costcodemo.wms.core.legacy;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Item master. Physical file WMSDTA/ITEMMAST.
 */
@Entity
@Table(name = "ITEMMAST")
public class ItemmastRecord {

    @Id
    @Column(name = "IMITEM", length = 10, nullable = false)
    private String itemNumber;

    @Column(name = "IMDESC", length = 30, nullable = false)
    private String description;

    @Column(name = "IMDEPT", length = 4)
    private String department;

    @Column(name = "IMUOM", length = 2, nullable = false)
    private String unitOfMeasure;

    @Column(name = "IMPRICE", precision = 11, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "IMSTAT", length = 1, nullable = false)
    private String statusFlag;

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(String statusFlag) {
        this.statusFlag = statusFlag;
    }
}
