package com.costcodemo.wms.terminal.screen.handler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.costcodemo.wms.core.domain.Order;
import com.costcodemo.wms.core.domain.OrderStatus;
import com.costcodemo.wms.core.service.OrderService;
import com.costcodemo.wms.terminal.screen.ScreenChrome;
import com.costcodemo.wms.terminal.screen.ScreenColor;
import com.costcodemo.wms.terminal.screen.ScreenField;
import com.costcodemo.wms.terminal.screen.ScreenHandler;
import com.costcodemo.wms.terminal.screen.TerminalScreen;
import com.costcodemo.wms.terminal.session.AidKey;
import com.costcodemo.wms.terminal.session.TerminalInput;
import com.costcodemo.wms.terminal.session.TerminalSession;

/**
 * WMS310 — order inquiry.
 *
 * <p>Orders are listed newest first, which matters for the demonstration: an order written
 * through the REST tier lands at the top of this subfile on the next refresh. There is one
 * ORDHDR file and both tiers read it.
 */
@Component
public class OrderInquiryHandler implements ScreenHandler {

    private static final int PAGE_SIZE = 10;
    private static final int FIRST_ROW = 10;
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MM/dd/yy");

    private final OrderService orderService;
    private final ScreenChrome chrome;

    public OrderInquiryHandler(OrderService orderService, ScreenChrome chrome) {
        this.orderService = orderService;
        this.chrome = chrome;
    }

    @Override
    public String screenId() {
        return "WMS310";
    }

    @Override
    public TerminalScreen render(TerminalSession session) {
        TerminalScreen screen = new TerminalScreen(screenId());
        chrome.drawHeader(screen, "Order Inquiry", LocalDateTime.now());

        screen.getBuffer().write(4, 2, "Member number . . . . .", ScreenColor.GREEN);
        screen.addField(new ScreenField("MBRFILT", 4, 27, 12, "", ScreenColor.WHITE, false));
        screen.getBuffer().write(4, 42, "(blank for all members)", ScreenColor.BLUE);

        screen.getBuffer().write(6, 2, "Type options, press Enter.", ScreenColor.TURQUOISE);
        screen.getBuffer().write(7, 4, "5=Display lines", ScreenColor.TURQUOISE);

        screen.getBuffer().write(9, 2, "Opt", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 6, "Order No", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 18, "Member No", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 32, "Whs", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 38, "St", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 42, "Ordered", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 53, "Lines", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 63, "Order Total", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 77, "Src", ScreenColor.TURQUOISE);

        List<Order> orders = orderService.findAll();
        int offset = clampOffset(session.getSubfileOffset(), orders.size());
        session.setSubfileOffset(offset);

        int row = FIRST_ROW;
        for (int i = offset; i < orders.size() && row < FIRST_ROW + PAGE_SIZE; i++, row++) {
            Order order = orders.get(i);
            screen.addField(new ScreenField("OPT" + i, row, 2, 2, "", ScreenColor.WHITE, false));
            screen.getBuffer().write(row, 6, order.getOrderNumber(), ScreenColor.WHITE);
            screen.getBuffer().write(row, 18, order.getMemberNumber(), ScreenColor.GREEN);
            screen.getBuffer().write(row, 32, order.getWarehouseCode(), ScreenColor.GREEN);
            screen.getBuffer().write(row, 38, order.getStatus().getLegacyCode(),
                    statusColor(order.getStatus()));
            screen.getBuffer().write(row, 42, format(order.getOrderDate()), ScreenColor.GREEN);
            screen.getBuffer().writeRight(row, 57, String.valueOf(order.getLineCount()),
                    ScreenColor.GREEN);
            screen.getBuffer().writeRight(row, 73,
                    String.format("%,.2f", order.getOrderTotal()), ScreenColor.YELLOW);
            // WEB orders are highlighted: they are the ones the digital tier wrote.
            screen.getBuffer().write(row, 77, order.getSourceCode(),
                    "WEB".equals(order.getSourceCode()) ? ScreenColor.TURQUOISE : ScreenColor.GREEN);
        }

        if (orders.isEmpty()) {
            screen.getBuffer().write(FIRST_ROW, 6, "(No orders on file)", ScreenColor.GREEN);
        }

        boolean atBottom = offset + PAGE_SIZE >= orders.size();
        screen.getBuffer().writeRight(21, 79, atBottom ? "Bottom" : "More...", ScreenColor.BLUE);

        chrome.drawLegend(screen, "F3=Exit   F5=Refresh   F7=Roll down   F8=Roll up   F12=Cancel");

        String pending = session.consumePendingMessage();
        if (!pending.isEmpty()) {
            if (session.isPendingMessageError()) {
                screen.setError(pending, "MBRFILT");
            } else {
                screen.setInfo(pending);
            }
        }
        chrome.drawMessage(screen);
        return screen;
    }

    /** Exception statuses in red, shipped in green, everything in flight in yellow. */
    private ScreenColor statusColor(OrderStatus status) {
        if (status.isException()) {
            return ScreenColor.RED;
        }
        return status == OrderStatus.SHIPPED ? ScreenColor.GREEN : ScreenColor.YELLOW;
    }

    @Override
    public void handle(TerminalSession session, TerminalInput input) {
        AidKey key = input.getAidKey();
        List<Order> orders = orderService.findAll();

        if (key == AidKey.F5) {
            session.setSubfileOffset(0);
            session.setPendingMessage("Display refreshed.", false);
            return;
        }
        if (key == AidKey.F8) {
            int next = session.getSubfileOffset() + PAGE_SIZE;
            if (next < orders.size()) {
                session.setSubfileOffset(next);
            }
            return;
        }
        if (key == AidKey.F7) {
            session.setSubfileOffset(Math.max(session.getSubfileOffset() - PAGE_SIZE, 0));
            return;
        }

        for (int i = 0; i < orders.size(); i++) {
            String option = input.get("OPT" + i);
            if (option.isEmpty()) {
                continue;
            }
            if ("5".equals(option)) {
                session.setSelectedOrderNumber(orders.get(i).getOrderNumber());
                session.pushScreen("WMS311");
            } else {
                session.setPendingMessage("Option " + option
                        + " is not valid.  Type a valid option from the list above.", true);
            }
            return;
        }

        String memberFilter = input.get("MBRFILT");
        if (!memberFilter.isEmpty()) {
            List<Order> filtered = orderService.findByMember(memberFilter);
            if (filtered.isEmpty()) {
                session.setPendingMessage(
                        "No orders on file for member " + memberFilter + ".", true);
            } else {
                session.setSelectedOrderNumber(filtered.get(0).getOrderNumber());
                session.pushScreen("WMS311");
            }
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
}
