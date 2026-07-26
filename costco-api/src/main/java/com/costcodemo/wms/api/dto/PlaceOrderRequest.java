package com.costcodemo.wms.api.dto;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * A new order arriving from the digital tier.
 *
 * <p>Field lengths mirror the underlying physical files — a member number longer than the
 * twelve characters MBRMAST.MBMBRN holds could never match a record, so it is rejected here
 * rather than failing deeper in.
 */
public record PlaceOrderRequest(

        @NotBlank(message = "Member number is required.")
        @Size(max = 12, message = "Member number cannot exceed 12 characters.")
        String memberNumber,

        @NotBlank(message = "Warehouse is required.")
        @Size(max = 4, message = "Warehouse code cannot exceed 4 characters.")
        String warehouseCode,

        @NotEmpty(message = "An order must have at least one line.")
        @Valid
        List<PlaceOrderLineRequest> lines) {
}
