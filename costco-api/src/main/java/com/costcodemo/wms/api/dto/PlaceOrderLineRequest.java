package com.costcodemo.wms.api.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record PlaceOrderLineRequest(

        @NotBlank(message = "Item number is required.")
        @Size(max = 10, message = "Item number cannot exceed 10 characters.")
        String itemNumber,

        @Min(value = 1, message = "Quantity must be greater than zero.")
        int quantity) {
}
