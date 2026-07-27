package com.costcodemo.wms.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.costcodemo.wms.api.dto.OrderResponse;
import com.costcodemo.wms.api.dto.PlaceOrderLineRequest;
import com.costcodemo.wms.api.dto.PlaceOrderRequest;
import com.costcodemo.wms.core.service.OrderLineRequest;
import com.costcodemo.wms.core.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Order endpoints.
 *
 * <p>A POST here writes ORDHDR and ORDDTL and allocates against INVBAL — the same records
 * the terminal's order screens read. Placing an order through this endpoint and then
 * refreshing WMS310 shows it at the top of the subfile.
 */
@Tag(name = "Orders")
@RestController
@RequestMapping("/api/orders")
public class OrderRestController {

    private final OrderService orderService;
    private final ApiMapper mapper;

    public OrderRestController(OrderService orderService, ApiMapper mapper) {
        this.orderService = orderService;
        this.mapper = mapper;
    }

    @Operation(summary = "List orders, optionally filtered by member or warehouse")
    @GetMapping
    public List<OrderResponse> list(@RequestParam(required = false) String member,
                                    @RequestParam(required = false) String warehouse) {
        if (member != null && !member.trim().isEmpty()) {
            return mapper.toOrderResponses(orderService.findByMember(member));
        }
        if (warehouse != null && !warehouse.trim().isEmpty()) {
            return mapper.toOrderResponses(orderService.findByWarehouse(warehouse));
        }
        return mapper.toOrderResponses(orderService.findAll());
    }

    @Operation(summary = "Retrieve a single order with its lines")
    @GetMapping("/{orderNumber}")
    public OrderResponse get(@PathVariable String orderNumber) {
        return mapper.toResponse(orderService.requireByNumber(orderNumber));
    }

    @Operation(summary = "Place an order, writing it to the core and allocating stock")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody PlaceOrderRequest request) {
        List<OrderLineRequest> lines = new ArrayList<>();
        for (PlaceOrderLineRequest line : request.lines()) {
            lines.add(new OrderLineRequest(line.itemNumber(), line.quantity()));
        }
        return mapper.toResponse(orderService.placeOrder(
                request.memberNumber(), request.warehouseCode(), lines, LocalDate.now()));
    }
}
