package com.costcodemo.wms.core.legacy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Warehouse master. Physical file WMSDTA/WHSMAST.
 */
@Entity
@Table(name = "WHSMAST")
public class WhsmastRecord {

    @Id
    @Column(name = "WHWHS", length = 4, nullable = false)
    private String warehouseCode;

    @Column(name = "WHNAME", length = 30, nullable = false)
    private String name;

    @Column(name = "WHCITY", length = 20)
    private String city;

    @Column(name = "WHSTAT", length = 2)
    private String state;

    @Column(name = "WHCTRY", length = 3)
    private String country;

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
