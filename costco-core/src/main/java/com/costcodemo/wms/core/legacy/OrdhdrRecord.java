package com.costcodemo.wms.core.legacy;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Order header. Physical file WMSDTA/ORDHDR.
 *
 * <p>OHTOTL is maintained by the order-entry program on write. It is not authoritative —
 * the domain layer recomputes the total from the detail lines, because any line changed
 * outside that program leaves the stored header total stale.
 */
@Entity
@Table(name = "ORDHDR")
public class OrdhdrRecord {

    @Id
    @Column(name = "OHORDN", length = 10, nullable = false)
    private String orderNumber;

    @Column(name = "OHMBRN", length = 12, nullable = false)
    private String memberNumber;

    @Column(name = "OHWHS", length = 4, nullable = false)
    private String warehouseCode;

    @Column(name = "OHSTAT", length = 2, nullable = false)
    private String statusCode;

    @Column(name = "OHORDT", nullable = false)
    private Integer orderDate;

    @Column(name = "OHTOTL", precision = 13, scale = 2)
    private BigDecimal storedTotal;

    @Column(name = "OHSRC", length = 3)
    private String sourceCode;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Integer orderDate) {
        this.orderDate = orderDate;
    }

    public BigDecimal getStoredTotal() {
        return storedTotal;
    }

    public void setStoredTotal(BigDecimal storedTotal) {
        this.storedTotal = storedTotal;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
