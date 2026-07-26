package com.costcodemo.wms.api.dto;

import java.math.BigDecimal;

public record ItemResponse(
        String itemNumber,
        String description,
        String department,
        String unitOfMeasure,
        BigDecimal price,
        boolean active) {
}
