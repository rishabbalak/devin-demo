package com.costcodemo.wms.core.legacy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Member master. Physical file WMSDTA/MBRMAST.
 *
 * <p>Field names follow the house standard on this system: a two-character file prefix
 * (MB) plus an abbreviated noun, capped at ten characters.
 */
@Entity
@Table(name = "MBRMAST")
public class MbrmastRecord {

    @Id
    @Column(name = "MBMBRN", length = 12, nullable = false)
    private String memberNumber;

    @Column(name = "MBNAME", length = 40, nullable = false)
    private String name;

    @Column(name = "MBTIER", length = 2, nullable = false)
    private String tierCode;

    @Column(name = "MBSTAT", length = 1, nullable = false)
    private String statusFlag;

    @Column(name = "MBJOIN", nullable = false)
    private Integer joinDate;

    @Column(name = "MBRNWD", nullable = false)
    private Integer renewalDate;

    @Column(name = "MBWHS", length = 4)
    private String homeWarehouse;

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTierCode() {
        return tierCode;
    }

    public void setTierCode(String tierCode) {
        this.tierCode = tierCode;
    }

    public String getStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(String statusFlag) {
        this.statusFlag = statusFlag;
    }

    public Integer getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Integer joinDate) {
        this.joinDate = joinDate;
    }

    public Integer getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(Integer renewalDate) {
        this.renewalDate = renewalDate;
    }

    public String getHomeWarehouse() {
        return homeWarehouse;
    }

    public void setHomeWarehouse(String homeWarehouse) {
        this.homeWarehouse = homeWarehouse;
    }
}
