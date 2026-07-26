package com.costcodemo.wms.core.domain;

import java.time.LocalDate;

/**
 * A membership account, translated out of the MBRMAST physical file.
 */
public class Member {

    private final String memberNumber;
    private final String name;
    private final MembershipTier tier;
    private final boolean active;
    private final LocalDate joinDate;
    private final LocalDate renewalDate;
    private final String homeWarehouse;

    public Member(String memberNumber, String name, MembershipTier tier, boolean active,
                  LocalDate joinDate, LocalDate renewalDate, String homeWarehouse) {
        this.memberNumber = memberNumber;
        this.name = name;
        this.tier = tier;
        this.active = active;
        this.joinDate = joinDate;
        this.renewalDate = renewalDate;
        this.homeWarehouse = homeWarehouse;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public String getName() {
        return name;
    }

    public MembershipTier getTier() {
        return tier;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public LocalDate getRenewalDate() {
        return renewalDate;
    }

    public String getHomeWarehouse() {
        return homeWarehouse;
    }

    /**
     * A membership is renewable inside the 60 days before it lapses, and stays renewable
     * after expiry — Costco does not force a new signup for a lapsed member.
     */
    public boolean isRenewable(LocalDate asOf) {
        if (renewalDate == null) {
            return false;
        }
        return !asOf.isBefore(renewalDate.minusDays(60));
    }

    public boolean isExpired(LocalDate asOf) {
        return renewalDate != null && asOf.isAfter(renewalDate);
    }
}
