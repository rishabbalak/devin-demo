package com.costcodemo.wms.core.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An order with its lines, assembled from the ORDHDR and ORDDTL physical files.
 *
 * <p>The two files are joined on order number in application code rather than by the
 * database. DB2/400 files here carry no declared foreign keys, which is typical of a
 * schema that grew up under RPG programs that did their own record-level access.
 */
public class Order {

    private final String orderNumber;
    private final String memberNumber;
    private final String warehouseCode;
    private final OrderStatus status;
    private final LocalDate orderDate;
    private final List<OrderLine> lines;

    public Order(String orderNumber, String memberNumber, String warehouseCode,
                 OrderStatus status, LocalDate orderDate, List<OrderLine> lines) {
        this.orderNumber = orderNumber;
        this.memberNumber = memberNumber;
        this.warehouseCode = warehouseCode;
        this.status = status;
        this.orderDate = orderDate;
        this.lines = lines == null ? new ArrayList<>() : new ArrayList<>(lines);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public int getLineCount() {
        return lines.size();
    }

    /**
     * Recomputed from the lines rather than read from ORDHDR. The header carries a stored
     * total that RPG updates on write, and it drifts when a line is changed outside the
     * order-entry program — so the total shown is always the one derived here.
     */
    public BigDecimal getOrderTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            total = total.add(line.getExtendedAmount());
        }
        return total;
    }

    public boolean hasBackorderedLines() {
        for (OrderLine line : lines) {
            if (line.isBackordered()) {
                return true;
            }
        }
        return false;
    }
}
