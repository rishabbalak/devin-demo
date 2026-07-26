package com.costcodemo.wms.core.domain;

/**
 * A warehouse location, translated out of the WHSMAST physical file.
 */
public class Warehouse {

    private final String code;
    private final String name;
    private final String city;
    private final String state;
    private final String country;

    public Warehouse(String code, String name, String city, String state, String country) {
        this.code = code;
        this.name = name;
        this.city = city;
        this.state = state;
        this.country = country;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }
}
