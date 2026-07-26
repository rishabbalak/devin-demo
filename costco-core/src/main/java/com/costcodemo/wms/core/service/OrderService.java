package com.costcodemo.wms.core.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.costcodemo.wms.core.domain.Order;
import com.costcodemo.wms.core.domain.OrderStatus;
import com.costcodemo.wms.core.legacy.InvbalRecord;
import com.costcodemo.wms.core.legacy.ItemmastRecord;
import com.costcodemo.wms.core.legacy.LegacyDateCodec;
import com.costcodemo.wms.core.legacy.LegacyRecordTranslator;
import com.costcodemo.wms.core.legacy.MbrmastRecord;
import com.costcodemo.wms.core.legacy.OrddtlRecord;
import com.costcodemo.wms.core.legacy.OrdhdrRecord;
import com.costcodemo.wms.core.repository.InvbalRepository;
import com.costcodemo.wms.core.repository.ItemmastRepository;
import com.costcodemo.wms.core.repository.MbrmastRepository;
import com.costcodemo.wms.core.repository.OrddtlRepository;
import com.costcodemo.wms.core.repository.OrdhdrRepository;
import com.costcodemo.wms.core.repository.WhsmastRepository;

/**
 * Order inquiry and entry against ORDHDR and ORDDTL.
 *
 * <p>Orders written here are the same records the terminal's WMS310 screen reads, so an
 * order placed through the REST tier shows up in the green screen subfile on the next
 * refresh. There is one system of record, not two.
 */
@Service
public class OrderService {

    /** Order numbers are ten-character right-aligned numerics, as ORDHDR.OHORDN expects. */
    private static final String ORDER_NUMBER_FORMAT = "%010d";

    private final OrdhdrRepository headerRepository;
    private final OrddtlRepository detailRepository;
    private final ItemmastRepository itemRepository;
    private final InvbalRepository balanceRepository;
    private final MbrmastRepository memberRepository;
    private final WhsmastRepository warehouseRepository;
    private final LegacyRecordTranslator translator;

    public OrderService(OrdhdrRepository headerRepository,
                        OrddtlRepository detailRepository,
                        ItemmastRepository itemRepository,
                        InvbalRepository balanceRepository,
                        MbrmastRepository memberRepository,
                        WhsmastRepository warehouseRepository,
                        LegacyRecordTranslator translator) {
        this.headerRepository = headerRepository;
        this.detailRepository = detailRepository;
        this.itemRepository = itemRepository;
        this.balanceRepository = balanceRepository;
        this.memberRepository = memberRepository;
        this.warehouseRepository = warehouseRepository;
        this.translator = translator;
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return headerRepository.findById(orderNumber.trim()).map(this::assemble);
    }

    @Transactional(readOnly = true)
    public Order requireByNumber(String orderNumber) {
        return findByNumber(orderNumber).orElseThrow(() -> new RecordNotFoundException(
                "USR0203", "Order number " + orderNumber + " not found."));
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return assembleAll(headerRepository.findAllByOrderByOrderNumberDesc());
    }

    @Transactional(readOnly = true)
    public List<Order> findByMember(String memberNumber) {
        return assembleAll(headerRepository.findByMemberNumberOrderByOrderNumberDesc(
                memberNumber == null ? "" : memberNumber.trim()));
    }

    @Transactional(readOnly = true)
    public List<Order> findByWarehouse(String warehouseCode) {
        return assembleAll(headerRepository.findByWarehouseCodeOrderByOrderNumberDesc(
                warehouseCode == null ? "" : warehouseCode.trim().toUpperCase()));
    }

    /**
     * Writes a new order and allocates stock against it.
     *
     * <p>Allocation walks the item's nettable locations in location order and takes what it
     * can from each. A line that cannot be filled completely is still accepted and carries a
     * backordered remainder, which is what puts the whole order into BO status.
     */
    @Transactional
    public Order placeOrder(String memberNumber, String warehouseCode,
                            List<OrderLineRequest> requestedLines, LocalDate orderDate) {

        String member = requireText(memberNumber, "USR0101", "Member number is required.");
        String warehouse = requireText(warehouseCode, "USR0110", "Warehouse is required.").toUpperCase();

        if (requestedLines == null || requestedLines.isEmpty()) {
            throw new OrderValidationException("USR0205", "An order must have at least one line.");
        }

        MbrmastRecord memberRecord = memberRepository.findById(member).orElseThrow(
                () -> new OrderValidationException("USR0101", "Member number " + member + " not found."));
        if (!"A".equalsIgnoreCase(trim(memberRecord.getStatusFlag()))) {
            throw new OrderValidationException("USR0102",
                    "Membership " + member + " is not active.  Order cannot be accepted.");
        }
        if (!warehouseRepository.existsById(warehouse)) {
            throw new OrderValidationException("USR0110", "Warehouse " + warehouse + " is not defined.");
        }

        OrdhdrRecord header = new OrdhdrRecord();
        header.setOrderNumber(nextOrderNumber());
        header.setMemberNumber(member);
        header.setWarehouseCode(warehouse);
        header.setOrderDate(LegacyDateCodec.fromLocalDate(orderDate));
        header.setSourceCode("WEB");

        List<OrddtlRecord> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean anyBackordered = false;
        int lineNumber = 10;

        for (OrderLineRequest requested : requestedLines) {
            String itemNumber = requireText(requested.getItemNumber(), "USR0142",
                    "Item number is required.").toUpperCase();
            if (requested.getQuantity() <= 0) {
                throw new OrderValidationException("USR0144",
                        "Quantity for item " + itemNumber + " must be greater than zero.");
            }
            ItemmastRecord item = itemRepository.findById(itemNumber).orElseThrow(
                    () -> new OrderValidationException("USR0142",
                            "Item number " + itemNumber + " not found in warehouse " + warehouse + "."));
            if (!"A".equalsIgnoreCase(trim(item.getStatusFlag()))) {
                throw new OrderValidationException("USR0143",
                        "Item " + itemNumber + " is not active and cannot be ordered.");
            }

            int allocated = allocate(warehouse, itemNumber, requested.getQuantity());
            if (allocated < requested.getQuantity()) {
                anyBackordered = true;
            }

            OrddtlRecord detail = new OrddtlRecord();
            detail.setOrderNumber(header.getOrderNumber());
            detail.setLineNumber(lineNumber);
            detail.setItemNumber(itemNumber);
            detail.setQuantityOrdered(requested.getQuantity());
            detail.setQuantityAllocated(allocated);
            detail.setUnitOfMeasure(trim(item.getUnitOfMeasure()));
            detail.setUnitPrice(item.getPrice());
            details.add(detail);

            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(requested.getQuantity())));
            lineNumber += 10;
        }

        header.setStatusCode(anyBackordered
                ? OrderStatus.BACKORDER.getLegacyCode()
                : OrderStatus.ALLOCATED.getLegacyCode());
        header.setStoredTotal(total);

        headerRepository.save(header);
        detailRepository.saveAll(details);

        return assemble(header);
    }

    /**
     * Takes up to {@code quantity} from the item's nettable locations, updating IBQALC as it
     * goes. Returns how much it managed to allocate.
     */
    private int allocate(String warehouseCode, String itemNumber, int quantity) {
        int remaining = quantity;
        List<InvbalRecord> balances =
                balanceRepository.findByWarehouseCodeAndItemNumberOrderByLocation(warehouseCode, itemNumber);

        for (InvbalRecord balance : balances) {
            if (remaining <= 0) {
                break;
            }
            String status = trim(balance.getStatusCode());
            if ("QC".equals(status) || "DM".equals(status)) {
                continue;
            }
            int available = balance.getQuantityOnHand() - balance.getQuantityAllocated();
            if (available <= 0) {
                continue;
            }
            int take = Math.min(available, remaining);
            balance.setQuantityAllocated(balance.getQuantityAllocated() + take);
            balanceRepository.save(balance);
            remaining -= take;
        }
        return quantity - remaining;
    }

    /**
     * Next sequential order number. The legacy system reads the highest key on file and adds
     * one rather than using a sequence object, so a gap is never reused.
     */
    private String nextOrderNumber() {
        long highest = 0L;
        for (OrdhdrRecord record : headerRepository.findAll()) {
            try {
                highest = Math.max(highest, Long.parseLong(trim(record.getOrderNumber())));
            } catch (NumberFormatException ignored) {
                // Non-numeric keys predate the current numbering scheme and are skipped.
            }
        }
        return String.format(ORDER_NUMBER_FORMAT, highest + 1);
    }

    private Order assemble(OrdhdrRecord header) {
        List<OrddtlRecord> details =
                detailRepository.findByOrderNumberOrderByLineNumber(trim(header.getOrderNumber()));
        Map<String, ItemmastRecord> itemsByNumber = new HashMap<>();
        for (OrddtlRecord detail : details) {
            String itemNumber = trim(detail.getItemNumber());
            if (!itemsByNumber.containsKey(itemNumber)) {
                itemRepository.findById(itemNumber)
                        .ifPresent(item -> itemsByNumber.put(itemNumber, item));
            }
        }
        return translator.toOrder(header, details, itemsByNumber);
    }

    private List<Order> assembleAll(List<OrdhdrRecord> headers) {
        List<Order> orders = new ArrayList<>();
        for (OrdhdrRecord header : headers) {
            orders.add(assemble(header));
        }
        return orders;
    }

    private String requireText(String value, String messageId, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new OrderValidationException(messageId, message);
        }
        return value.trim();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
