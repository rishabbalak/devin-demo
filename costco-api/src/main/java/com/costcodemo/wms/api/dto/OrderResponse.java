package com.costcodemo.wms.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * An order and its lines.
 *
 * <p>{@code orderTotal} is recomputed from the lines rather than taken from ORDHDR.OHTOTL,
 * which can drift when a line is maintained outside the order-entry program.
 */
public record OrderResponse(
        String orderNumber,
        String memberNumber,
        String warehouseCode,
        String status,
        String statusName,
        LocalDate orderDate,
        String source,
        int lineCount,
        BigDecimal orderTotal,
        boolean hasBackorderedLines,
        List<OrderLineResponse> lines) {
}
