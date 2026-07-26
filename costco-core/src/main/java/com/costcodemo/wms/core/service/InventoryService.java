package com.costcodemo.wms.core.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.costcodemo.wms.core.domain.InventoryBalance;
import com.costcodemo.wms.core.domain.Item;
import com.costcodemo.wms.core.domain.Warehouse;
import com.costcodemo.wms.core.legacy.InvbalRecord;
import com.costcodemo.wms.core.legacy.ItemmastRecord;
import com.costcodemo.wms.core.legacy.LegacyRecordTranslator;
import com.costcodemo.wms.core.legacy.WhsmastRecord;
import com.costcodemo.wms.core.repository.InvbalRepository;
import com.costcodemo.wms.core.repository.ItemmastRepository;
import com.costcodemo.wms.core.repository.WhsmastRepository;

/**
 * Item master and inventory balance inquiry, backing both the terminal's WMS210 screen and
 * the REST catalog endpoints.
 */
@Service
public class InventoryService {

    private final ItemmastRepository itemRepository;
    private final InvbalRepository balanceRepository;
    private final WhsmastRepository warehouseRepository;
    private final LegacyRecordTranslator translator;

    public InventoryService(ItemmastRepository itemRepository,
                            InvbalRepository balanceRepository,
                            WhsmastRepository warehouseRepository,
                            LegacyRecordTranslator translator) {
        this.itemRepository = itemRepository;
        this.balanceRepository = balanceRepository;
        this.warehouseRepository = warehouseRepository;
        this.translator = translator;
    }

    @Transactional(readOnly = true)
    public List<Warehouse> findAllWarehouses() {
        List<Warehouse> warehouses = new ArrayList<>();
        for (WhsmastRecord record : warehouseRepository.findAllByOrderByWarehouseCode()) {
            warehouses.add(translator.toWarehouse(record));
        }
        return warehouses;
    }

    @Transactional(readOnly = true)
    public Optional<Warehouse> findWarehouse(String warehouseCode) {
        if (warehouseCode == null || warehouseCode.trim().isEmpty()) {
            return Optional.empty();
        }
        return warehouseRepository.findById(warehouseCode.trim().toUpperCase())
                .map(translator::toWarehouse);
    }

    @Transactional(readOnly = true)
    public List<Item> findAllItems() {
        return translateItems(itemRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Optional<Item> findItem(String itemNumber) {
        if (itemNumber == null || itemNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return itemRepository.findById(itemNumber.trim().toUpperCase()).map(translator::toItem);
    }

    @Transactional(readOnly = true)
    public Item requireItem(String itemNumber) {
        return findItem(itemNumber).orElseThrow(() -> new RecordNotFoundException(
                "USR0142", "Item number " + itemNumber + " not found."));
    }

    @Transactional(readOnly = true)
    public List<Item> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return findAllItems();
        }
        return translateItems(itemRepository.searchByDescription(text.trim()));
    }

    @Transactional(readOnly = true)
    public List<InventoryBalance> findBalancesByWarehouse(String warehouseCode) {
        return translateBalances(
                balanceRepository.findByWarehouseCodeOrderByLocation(normalize(warehouseCode)));
    }

    /**
     * Loads the subfile from a starting location forward, which is what the terminal's
     * "Position to" field does — it repositions the list rather than filtering it.
     */
    @Transactional(readOnly = true)
    public List<InventoryBalance> findBalancesFromLocation(String warehouseCode, String location) {
        if (location == null || location.trim().isEmpty()) {
            return findBalancesByWarehouse(warehouseCode);
        }
        return translateBalances(
                balanceRepository.findByWarehouseCodeAndLocationGreaterThanEqualOrderByLocation(
                        normalize(warehouseCode), location.trim().toUpperCase()));
    }

    @Transactional(readOnly = true)
    public List<InventoryBalance> findBalancesForItem(String warehouseCode, String itemNumber) {
        return translateBalances(
                balanceRepository.findByWarehouseCodeAndItemNumberOrderByLocation(
                        normalize(warehouseCode), normalize(itemNumber)));
    }

    /**
     * Total quantity a warehouse can actually promise for an item. Quality-hold and damaged
     * locations carry real on-hand quantities but are excluded — that exclusion is the whole
     * reason availability cannot simply be summed from IBQOH.
     */
    @Transactional(readOnly = true)
    public int availableQuantity(String warehouseCode, String itemNumber) {
        int available = 0;
        for (InventoryBalance balance : findBalancesForItem(warehouseCode, itemNumber)) {
            if (balance.isNettable()) {
                available += balance.getQuantityAvailable();
            }
        }
        return available;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private List<Item> translateItems(List<ItemmastRecord> records) {
        List<Item> items = new ArrayList<>();
        for (ItemmastRecord record : records) {
            items.add(translator.toItem(record));
        }
        return items;
    }

    private List<InventoryBalance> translateBalances(List<InvbalRecord> records) {
        List<InventoryBalance> balances = new ArrayList<>();
        for (InvbalRecord record : records) {
            balances.add(translator.toInventoryBalance(record));
        }
        return balances;
    }
}
