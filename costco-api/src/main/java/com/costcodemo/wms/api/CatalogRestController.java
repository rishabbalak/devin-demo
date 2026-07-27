package com.costcodemo.wms.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.costcodemo.wms.api.dto.InventoryResponse;
import com.costcodemo.wms.api.dto.ItemResponse;
import com.costcodemo.wms.core.service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Catalog and inventory availability, read from ITEMMAST and INVBAL.
 */
@Tag(name = "Catalog")
@RestController
public class CatalogRestController {

    private final InventoryService inventoryService;
    private final ApiMapper mapper;

    public CatalogRestController(InventoryService inventoryService, ApiMapper mapper) {
        this.inventoryService = inventoryService;
        this.mapper = mapper;
    }

    @Operation(summary = "List catalog items, optionally filtered by description")
    @GetMapping("/api/catalog/items")
    public List<ItemResponse> items(@RequestParam(required = false) String search) {
        return mapper.toItemResponses(inventoryService.searchItems(search));
    }

    @Operation(summary = "Retrieve a single item by item number")
    @GetMapping("/api/catalog/items/{itemNumber}")
    public ItemResponse item(@PathVariable String itemNumber) {
        return mapper.toResponse(inventoryService.requireItem(itemNumber));
    }

    @Operation(summary = "List warehouses")
    @GetMapping("/api/catalog/warehouses")
    public List<?> warehouses() {
        return inventoryService.findAllWarehouses();
    }

    @Operation(summary = "Inventory balances for a warehouse, optionally narrowed to one item")
    @GetMapping("/api/inventory")
    public List<InventoryResponse> inventory(@RequestParam String warehouse,
                                             @RequestParam(required = false) String item) {
        if (item != null && !item.trim().isEmpty()) {
            return mapper.toInventoryResponses(
                    inventoryService.findBalancesForItem(warehouse, item));
        }
        return mapper.toInventoryResponses(inventoryService.findBalancesByWarehouse(warehouse));
    }

    @Operation(summary = "Total promisable quantity for an item in a warehouse")
    @GetMapping("/api/inventory/available")
    public int available(@RequestParam String warehouse, @RequestParam String item) {
        return inventoryService.availableQuantity(warehouse, item);
    }
}
