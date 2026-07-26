package com.costcodemo.wms.api.dto;

import java.time.LocalDate;

public record MemberResponse(
        String memberNumber,
        String name,
        String tier,
        String tierName,
        int annualFeeUsd,
        boolean rewardEligible,
        boolean active,
        LocalDate joinDate,
        LocalDate renewalDate,
        String homeWarehouse) {
}
