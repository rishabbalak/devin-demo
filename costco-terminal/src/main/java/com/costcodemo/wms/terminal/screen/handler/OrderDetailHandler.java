package com.costcodemo.wms.terminal.screen.handler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.costcodemo.wms.core.domain.Member;
import com.costcodemo.wms.core.domain.Order;
import com.costcodemo.wms.core.domain.OrderLine;
import com.costcodemo.wms.core.domain.OrderStatus;
import com.costcodemo.wms.core.service.MemberService;
import com.costcodemo.wms.core.service.OrderService;
import com.costcodemo.wms.terminal.screen.ScreenChrome;
import com.costcodemo.wms.terminal.screen.ScreenColor;
import com.costcodemo.wms.terminal.screen.ScreenHandler;
import com.costcodemo.wms.terminal.screen.TerminalScreen;
import com.costcodemo.wms.terminal.session.AidKey;
import com.costcodemo.wms.terminal.session.TerminalInput;
import com.costcodemo.wms.terminal.session.TerminalSession;

/**
 * WMS311 — order detail lines.
 *
 * <p>The order total shown at the foot is recomputed from the lines rather than read from
 * ORDHDR.OHTOTL, so it stays correct even when the stored header total has drifted.
 */
@Component
public class OrderDetailHandler implements ScreenHandler {

    private static final int PAGE_SIZE = 10;
    private static final int FIRST_ROW = 10;
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MM/dd/yy");

    private final OrderService orderService;
    private final MemberService memberService;
    private final ScreenChrome chrome;

    public OrderDetailHandler(OrderService orderService, MemberService memberService,
                              ScreenChrome chrome) {
        this.orderService = orderService;
        this.memberService = memberService;
        this.chrome = chrome;
    }

    @Override
    public String screenId() {
        return "WMS311";
    }

    @Override
    public TerminalScreen render(TerminalSession session) {
        TerminalScreen screen = new TerminalScreen(screenId());
        chrome.drawHeader(screen, "Order Detail", LocalDateTime.now());

        Optional<Order> maybeOrder = orderService.findByNumber(session.getSelectedOrderNumber());
        if (!maybeOrder.isPresent()) {
            screen.getBuffer().write(10, 6, "Order " + session.getSelectedOrderNumber()
                    + " is no longer on file.", ScreenColor.RED);
            chrome.drawLegend(screen, "F3=Exit   F12=Cancel");
            chrome.drawMessage(screen);
            return screen;
        }

        Order order = maybeOrder.get();
        drawOrderHeader(screen, order);
        drawLineHeadings(screen);

        List<OrderLine> lines = order.getLines();
        int offset = clampOffset(session.getSubfileOffset(), lines.size());
        session.setSubfileOffset(offset);

        int row = FIRST_ROW;
        for (int i = offset; i < lines.size() && row < FIRST_ROW + PAGE_SIZE; i++, row++) {
            drawLine(screen, row, lines.get(i));
        }

        boolean atBottom = offset + PAGE_SIZE >= lines.size();
        screen.getBuffer().writeRight(20, 79, atBottom ? "Bottom" : "More...", ScreenColor.BLUE);

        screen.getBuffer().write(21, 28, "Lines:", ScreenColor.TURQUOISE);
        screen.getBuffer().write(21, 36, String.valueOf(order.getLineCount()), ScreenColor.YELLOW);
        screen.getBuffer().write(21, 44, "Order total:", ScreenColor.TURQUOISE);
        screen.getBuffer().writeRight(21, 73,
                String.format("%,.2f", order.getOrderTotal()), ScreenColor.YELLOW);

        chrome.drawLegend(screen, "F3=Exit   F5=Refresh   F7=Roll down   F8=Roll up   F12=Cancel");

        String pending = session.consumePendingMessage();
        if (!pending.isEmpty()) {
            if (session.isPendingMessageError()) {
                screen.setError(pending, null);
            } else {
                screen.setInfo(pending);
            }
        } else if (order.hasBackorderedLines()) {
            screen.setInfo("One or more lines are backordered.  Review quantities below.");
        }
        chrome.drawMessage(screen);
        return screen;
    }

    private void drawOrderHeader(TerminalScreen screen, Order order) {
        screen.getBuffer().write(4, 2, "Order nbr . . :", ScreenColor.GREEN);
        screen.getBuffer().write(4, 19, order.getOrderNumber(), ScreenColor.WHITE);
        screen.getBuffer().write(4, 33, "Status . :", ScreenColor.GREEN);
        OrderStatus status = order.getStatus();
        screen.getBuffer().write(4, 45,
                status.getLegacyCode() + " " + status.getDisplayName(), statusColor(status));

        screen.getBuffer().write(5, 2, "Member  . . . :", ScreenColor.GREEN);
        screen.getBuffer().write(5, 19, order.getMemberNumber(), ScreenColor.GREEN);
        Optional<Member> member = memberService.findByNumber(order.getMemberNumber());
        screen.getBuffer().write(5, 33, truncate(member.map(Member::getName)
                .orElse("MEMBER NOT ON FILE"), 30), ScreenColor.GREEN);

        screen.getBuffer().write(6, 2, "Ordered . . . :", ScreenColor.GREEN);
        screen.getBuffer().write(6, 19, format(order.getOrderDate()), ScreenColor.GREEN);
        screen.getBuffer().write(6, 33, "Warehouse:", ScreenColor.GREEN);
        screen.getBuffer().write(6, 45, order.getWarehouseCode(), ScreenColor.GREEN);
        screen.getBuffer().write(6, 55, "Source :", ScreenColor.GREEN);
        screen.getBuffer().write(6, 65, order.getSourceCode(),
                "WEB".equals(order.getSourceCode()) ? ScreenColor.TURQUOISE : ScreenColor.GREEN);
    }

    private void drawLineHeadings(TerminalScreen screen) {
        screen.getBuffer().write(9, 6, "Ln", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 11, "Item No", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 23, "Description", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 45, "Ord Qty", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 54, "Alloc", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 61, "B/O", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 66, "UM", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 70, "Ext Amount", ScreenColor.TURQUOISE);
    }

    private void drawLine(TerminalScreen screen, int row, OrderLine line) {
        screen.getBuffer().write(row, 6, String.format("%03d", line.getLineNumber()),
                ScreenColor.GREEN);
        screen.getBuffer().write(row, 11, line.getItemNumber(), ScreenColor.WHITE);
        screen.getBuffer().write(row, 23, truncate(line.getDescription(), 20), ScreenColor.GREEN);
        screen.getBuffer().writeRight(row, 51, String.format("%,d", line.getQuantityOrdered()),
                ScreenColor.GREEN);
        screen.getBuffer().writeRight(row, 58, String.format("%,d", line.getQuantityAllocated()),
                ScreenColor.GREEN);

        int backordered = line.getQuantityBackordered();
        screen.getBuffer().writeRight(row, 63, String.valueOf(backordered),
                backordered > 0 ? ScreenColor.RED : ScreenColor.GREEN);

        screen.getBuffer().write(row, 66, line.getUnitOfMeasure(), ScreenColor.TURQUOISE);
        screen.getBuffer().writeRight(row, 79,
                String.format("%,.2f", line.getExtendedAmount()), ScreenColor.GREEN);
    }

    private ScreenColor statusColor(OrderStatus status) {
        if (status.isException()) {
            return ScreenColor.RED;
        }
        return status == OrderStatus.SHIPPED ? ScreenColor.GREEN : ScreenColor.YELLOW;
    }

    @Override
    public void handle(TerminalSession session, TerminalInput input) {
        AidKey key = input.getAidKey();
        Optional<Order> order = orderService.findByNumber(session.getSelectedOrderNumber());
        int lineCount = order.map(Order::getLineCount).orElse(0);

        if (key == AidKey.F8) {
            int next = session.getSubfileOffset() + PAGE_SIZE;
            if (next < lineCount) {
                session.setSubfileOffset(next);
            }
            return;
        }
        if (key == AidKey.F7) {
            session.setSubfileOffset(Math.max(session.getSubfileOffset() - PAGE_SIZE, 0));
            return;
        }
        if (key == AidKey.F5) {
            session.setSubfileOffset(0);
            session.setPendingMessage("Display refreshed.", false);
        }
    }

    private int clampOffset(int offset, int size) {
        if (size == 0) {
            return 0;
        }
        if (offset >= size) {
            return ((size - 1) / PAGE_SIZE) * PAGE_SIZE;
        }
        return Math.max(offset, 0);
    }

    private String format(LocalDate date) {
        return date == null ? "  /  /  " : date.format(SHORT_DATE);
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
