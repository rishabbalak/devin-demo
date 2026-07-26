package com.costcodemo.wms.terminal.screen.handler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.costcodemo.wms.core.domain.InventoryBalance;
import com.costcodemo.wms.core.domain.Item;
import com.costcodemo.wms.core.domain.Warehouse;
import com.costcodemo.wms.core.service.InventoryService;
import com.costcodemo.wms.terminal.screen.ScreenChrome;
import com.costcodemo.wms.terminal.screen.ScreenColor;
import com.costcodemo.wms.terminal.screen.ScreenField;
import com.costcodemo.wms.terminal.screen.ScreenHandler;
import com.costcodemo.wms.terminal.screen.TerminalScreen;
import com.costcodemo.wms.terminal.session.AidKey;
import com.costcodemo.wms.terminal.session.TerminalInput;
import com.costcodemo.wms.terminal.session.TerminalSession;

/**
 * WMS210 — inventory balance inquiry.
 *
 * <p>The classic subfile screen. "Position to location" repositions the list rather than
 * filtering it, F11 swaps to an alternate column set, and quantities carry colour: allocated
 * in yellow when non-zero, available in red when nothing can be promised.
 *
 * <p>Quality-hold and damaged locations show their real on-hand quantity but a zero
 * available, because those rows are not nettable. That distinction is invisible in the raw
 * file and is exactly the sort of rule that lives only in application code.
 */
@Component
public class InventoryInquiryHandler implements ScreenHandler {

    private static final int PAGE_SIZE = 9;
    private static final int FIRST_ROW = 11;

    private final InventoryService inventoryService;
    private final ScreenChrome chrome;

    public InventoryInquiryHandler(InventoryService inventoryService, ScreenChrome chrome) {
        this.inventoryService = inventoryService;
        this.chrome = chrome;
    }

    @Override
    public String screenId() {
        return "WMS210";
    }

    @Override
    public TerminalScreen render(TerminalSession session) {
        TerminalScreen screen = new TerminalScreen(screenId());
        chrome.drawHeader(screen, "Inventory Balance Inquiry", LocalDateTime.now());

        String warehouseCode = session.getWarehouseCode();
        screen.getBuffer().write(4, 2, "Warehouse . . . . . . .", ScreenColor.GREEN);
        screen.addField(new ScreenField("WHS", 4, 27, 4, warehouseCode, ScreenColor.WHITE, false));

        Optional<Warehouse> warehouse = inventoryService.findWarehouse(warehouseCode);
        screen.getBuffer().write(4, 34,
                warehouse.map(Warehouse::getName).orElse("** NOT DEFINED **"),
                warehouse.isPresent() ? ScreenColor.GREEN : ScreenColor.RED);

        screen.getBuffer().write(5, 2, "Position to location  .", ScreenColor.GREEN);
        screen.addField(new ScreenField("POSN", 5, 27, 9, "", ScreenColor.WHITE, false));

        screen.getBuffer().write(7, 2, "Type options, press Enter.", ScreenColor.TURQUOISE);
        screen.getBuffer().write(8, 4, "5=Display detail", ScreenColor.TURQUOISE);

        boolean alternate = session.isAlternateView();
        drawHeadings(screen, alternate);

        List<InventoryBalance> balances = inventoryService.findBalancesByWarehouse(warehouseCode);
        int offset = clampOffset(session.getSubfileOffset(), balances.size());
        session.setSubfileOffset(offset);

        int row = FIRST_ROW;
        for (int i = offset; i < balances.size() && row < FIRST_ROW + PAGE_SIZE; i++, row++) {
            drawRow(screen, row, i, balances.get(i), alternate);
        }

        if (balances.isEmpty()) {
            screen.getBuffer().write(FIRST_ROW, 6,
                    "(No inventory records on file for this warehouse)", ScreenColor.GREEN);
        }

        boolean atBottom = offset + PAGE_SIZE >= balances.size();
        screen.getBuffer().writeRight(21, 79, atBottom ? "Bottom" : "More...", ScreenColor.BLUE);

        chrome.drawLegend(screen,
                "F3=Exit  F5=Refresh  F7=Roll down  F8=Roll up  F11=Alternate view  F12=Cancel");

        String pending = session.consumePendingMessage();
        if (!pending.isEmpty()) {
            if (session.isPendingMessageError()) {
                screen.setError(pending, "POSN");
            } else {
                screen.setInfo(pending);
            }
        }
        chrome.drawMessage(screen);
        return screen;
    }

    private void drawHeadings(TerminalScreen screen, boolean alternate) {
        screen.getBuffer().write(10, 2, "Opt", ScreenColor.TURQUOISE);
        screen.getBuffer().write(10, 6, "Location", ScreenColor.TURQUOISE);
        screen.getBuffer().write(10, 17, "Item No", ScreenColor.TURQUOISE);
        screen.getBuffer().write(10, 29, "Description", ScreenColor.TURQUOISE);
        if (alternate) {
            screen.getBuffer().write(10, 53, "Dept", ScreenColor.TURQUOISE);
            screen.getBuffer().write(10, 60, "UM", ScreenColor.TURQUOISE);
            screen.getBuffer().write(10, 66, "Unit Price", ScreenColor.TURQUOISE);
        } else {
            screen.getBuffer().write(10, 53, "On-Hand", ScreenColor.TURQUOISE);
            screen.getBuffer().write(10, 62, " Alloc", ScreenColor.TURQUOISE);
            screen.getBuffer().write(10, 70, "   Avl", ScreenColor.TURQUOISE);
            screen.getBuffer().write(10, 78, "St", ScreenColor.TURQUOISE);
        }
    }

    private void drawRow(TerminalScreen screen, int row, int index,
                         InventoryBalance balance, boolean alternate) {
        screen.addField(new ScreenField("OPT" + index, row, 2, 2, "", ScreenColor.WHITE, false));

        boolean nettable = balance.isNettable();
        screen.getBuffer().write(row, 6, balance.getLocation(),
                nettable ? ScreenColor.GREEN : ScreenColor.RED);
        screen.getBuffer().write(row, 17, balance.getItemNumber(), ScreenColor.WHITE);

        Optional<Item> item = inventoryService.findItem(balance.getItemNumber());
        screen.getBuffer().write(row, 29,
                truncate(item.map(Item::getDescription).orElse("ITEM NOT ON FILE"), 22),
                ScreenColor.GREEN);

        if (alternate) {
            screen.getBuffer().write(row, 53, item.map(Item::getDepartment).orElse(""),
                    ScreenColor.GREEN);
            screen.getBuffer().write(row, 60, item.map(Item::getUnitOfMeasure).orElse(""),
                    ScreenColor.TURQUOISE);
            screen.getBuffer().writeRight(row, 75,
                    item.map(i -> String.format("%,.2f", i.getPrice())).orElse(""),
                    ScreenColor.GREEN);
            return;
        }

        screen.getBuffer().writeRight(row, 59, format(balance.getQuantityOnHand()), ScreenColor.GREEN);

        int allocated = balance.getQuantityAllocated();
        screen.getBuffer().writeRight(row, 67, format(allocated),
                allocated > 0 ? ScreenColor.YELLOW : ScreenColor.GREEN);

        int available = nettable ? balance.getQuantityAvailable() : 0;
        screen.getBuffer().writeRight(row, 75, format(available),
                available == 0 ? ScreenColor.RED : ScreenColor.GREEN);

        screen.getBuffer().write(row, 78, balance.getStatusCode(),
                nettable ? ScreenColor.GREEN : ScreenColor.RED);
    }

    @Override
    public void handle(TerminalSession session, TerminalInput input) {
        AidKey key = input.getAidKey();

        String requestedWarehouse = input.get("WHS");
        if (!requestedWarehouse.isEmpty() && !requestedWarehouse.equals(session.getWarehouseCode())) {
            if (inventoryService.findWarehouse(requestedWarehouse).isPresent()) {
                session.setWarehouseCode(requestedWarehouse);
                session.setSubfileOffset(0);
            } else {
                session.setPendingMessage(
                        "Warehouse " + requestedWarehouse + " is not defined.", true);
                return;
            }
        }

        if (key == AidKey.F11) {
            session.toggleAlternateView();
            return;
        }
        if (key == AidKey.F5) {
            session.setSubfileOffset(0);
            session.setPendingMessage("Display refreshed.", false);
            return;
        }

        List<InventoryBalance> balances =
                inventoryService.findBalancesByWarehouse(session.getWarehouseCode());

        if (key == AidKey.F8) {
            int next = session.getSubfileOffset() + PAGE_SIZE;
            if (next < balances.size()) {
                session.setSubfileOffset(next);
            }
            return;
        }
        if (key == AidKey.F7) {
            session.setSubfileOffset(Math.max(session.getSubfileOffset() - PAGE_SIZE, 0));
            return;
        }

        String position = input.get("POSN");
        if (!position.isEmpty()) {
            int index = indexOfLocation(balances, position);
            if (index < 0) {
                session.setPendingMessage("Location " + position
                        + " is not defined in warehouse " + session.getWarehouseCode() + ".", true);
            } else {
                session.setSubfileOffset(index);
            }
            return;
        }

        for (int i = 0; i < balances.size(); i++) {
            String option = input.get("OPT" + i);
            if (option.isEmpty()) {
                continue;
            }
            if ("5".equals(option)) {
                session.setPendingMessage(describe(balances.get(i)), false);
            } else {
                session.setPendingMessage("Option " + option
                        + " is not valid.  Type a valid option from the list above.", true);
            }
            return;
        }
    }

    /** First row at or after the requested location, which is what "position to" means. */
    private int indexOfLocation(List<InventoryBalance> balances, String location) {
        for (int i = 0; i < balances.size(); i++) {
            if (balances.get(i).getLocation().compareToIgnoreCase(location) >= 0) {
                return i;
            }
        }
        return -1;
    }

    private String describe(InventoryBalance balance) {
        if (!balance.isNettable()) {
            return balance.getLocation() + " is status " + balance.getStatusCode()
                    + " and is not nettable.  On hand " + format(balance.getQuantityOnHand())
                    + ", available 0.";
        }
        return balance.getLocation() + "  on hand " + format(balance.getQuantityOnHand())
                + ", allocated " + format(balance.getQuantityAllocated())
                + ", available " + format(balance.getQuantityAvailable()) + ".";
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

    /** Comma-grouped and unpadded, the effect of {@code EDTCDE(1)} on a numeric field. */
    private String format(int quantity) {
        return String.format("%,d", quantity);
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
