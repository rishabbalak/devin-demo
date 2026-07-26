package com.costcodemo.wms.api.dto;

import java.math.BigDecimal;

public record OrderLineResponse(
        int lineNumber,
        String itemNumber,
        String description,
        int quantityOrdered,
        int quantityAllocated,
        int quantityBackordered,
        String unitOfMeasure,
        BigDecimal unitPrice,
        BigDecimal extendedAmount) {
}
