package com.costcodemo.wms.core.legacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.costcodemo.wms.core.domain.InventoryBalance;
import com.costcodemo.wms.core.domain.Item;
import com.costcodemo.wms.core.domain.Member;
import com.costcodemo.wms.core.domain.MembershipTier;
import com.costcodemo.wms.core.domain.Order;
import com.costcodemo.wms.core.domain.OrderLine;
import com.costcodemo.wms.core.domain.OrderStatus;
import com.costcodemo.wms.core.domain.Warehouse;

/**
 * Translates DB2/400 record shapes into clean domain models.
 *
 * <p>This is the only place that knows about CYYMMDD dates, single-character status flags,
 * or the fixed-width padding that comes back from the physical files. Everything above
 * this layer works in {@code LocalDate}, enums and trimmed strings.
 */
@Component
public class LegacyRecordTranslator {

    /** Fixed-width CHAR columns come back space-padded; nothing above this layer wants that. */
    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    public Member toMember(MbrmastRecord record) {
        if (record == null) {
            return null;
        }
        return new Member(
                clean(record.getMemberNumber()),
                clean(record.getName()),
                MembershipTier.fromLegacyCode(record.getTierCode()),
                "A".equalsIgnoreCase(clean(record.getStatusFlag())),
                LegacyDateCodec.toLocalDate(record.getJoinDate()),
                LegacyDateCodec.toLocalDate(record.getRenewalDate()),
                clean(record.getHomeWarehouse()));
    }

    public Warehouse toWarehouse(WhsmastRecord record) {
        if (record == null) {
            return null;
        }
        return new Warehouse(
                clean(record.getWarehouseCode()),
                clean(record.getName()),
                clean(record.getCity()),
                clean(record.getState()),
                clean(record.getCountry()));
    }

    public Item toItem(ItemmastRecord record) {
        if (record == null) {
            return null;
        }
        return new Item(
                clean(record.getItemNumber()),
                clean(record.getDescription()),
                clean(record.getDepartment()),
                clean(record.getUnitOfMeasure()),
                record.getPrice(),
                "A".equalsIgnoreCase(clean(record.getStatusFlag())));
    }

    public InventoryBalance toInventoryBalance(InvbalRecord record) {
        if (record == null) {
            return null;
        }
        return new InventoryBalance(
                clean(record.getWarehouseCode()),
                clean(record.getItemNumber()),
                clean(record.getLocation()),
                record.getQuantityOnHand(),
                record.getQuantityAllocated(),
                clean(record.getStatusCode()));
    }

    /**
     * Assembles an order from its header and detail records.
     *
     * <p>ORDDTL carries no description of its own, so descriptions are supplied from the
     * item master. A line whose item has since been purged from ITEMMAST still has to
     * render on the terminal, so it falls back to a placeholder rather than failing.
     */
    public Order toOrder(OrdhdrRecord header, List<OrddtlRecord> details,
                         Map<String, ItemmastRecord> itemsByNumber) {
        if (header == null) {
            return null;
        }
        List<OrderLine> lines = new ArrayList<>();
        if (details != null) {
            for (OrddtlRecord detail : details) {
                lines.add(toOrderLine(detail, itemsByNumber));
            }
        }
        return new Order(
                clean(header.getOrderNumber()),
                clean(header.getMemberNumber()),
                clean(header.getWarehouseCode()),
                OrderStatus.fromLegacyCode(header.getStatusCode()),
                LegacyDateCodec.toLocalDate(header.getOrderDate()),
                clean(header.getSourceCode()),
                lines);
    }

    private OrderLine toOrderLine(OrddtlRecord detail, Map<String, ItemmastRecord> itemsByNumber) {
        String itemNumber = clean(detail.getItemNumber());
        String description = "ITEM NOT ON FILE";
        if (itemsByNumber != null) {
            ItemmastRecord item = itemsByNumber.get(itemNumber);
            if (item != null) {
                description = clean(item.getDescription());
            }
        }
        return new OrderLine(
                detail.getLineNumber(),
                itemNumber,
                description,
                detail.getQuantityOrdered(),
                detail.getQuantityAllocated(),
                clean(detail.getUnitOfMeasure()),
                detail.getUnitPrice());
    }
}
