package com.costcodemo.wms.terminal.screen.handler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.costcodemo.wms.core.domain.Member;
import com.costcodemo.wms.core.service.MemberService;
import com.costcodemo.wms.core.service.RecordNotFoundException;
import com.costcodemo.wms.terminal.screen.ScreenChrome;
import com.costcodemo.wms.terminal.screen.ScreenColor;
import com.costcodemo.wms.terminal.screen.ScreenField;
import com.costcodemo.wms.terminal.screen.ScreenHandler;
import com.costcodemo.wms.terminal.screen.TerminalScreen;
import com.costcodemo.wms.terminal.session.AidKey;
import com.costcodemo.wms.terminal.session.TerminalInput;
import com.costcodemo.wms.terminal.session.TerminalSession;

/**
 * WMS110 — member inquiry and renewal.
 *
 * <p>A load-all subfile over MBRMAST with an option column. Option 2 renews a membership,
 * which writes back to the file, so this screen both reads and updates the system of record.
 */
@Component
public class MemberInquiryHandler implements ScreenHandler {

    private static final int PAGE_SIZE = 10;
    private static final int FIRST_ROW = 11;
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MM/dd/yy");

    private final MemberService memberService;
    private final ScreenChrome chrome;

    public MemberInquiryHandler(MemberService memberService, ScreenChrome chrome) {
        this.memberService = memberService;
        this.chrome = chrome;
    }

    @Override
    public String screenId() {
        return "WMS110";
    }

    @Override
    public TerminalScreen render(TerminalSession session) {
        TerminalScreen screen = new TerminalScreen(screenId());
        chrome.drawHeader(screen, "Member Inquiry", LocalDateTime.now());

        screen.getBuffer().write(4, 2, "Position to name  . . . . .", ScreenColor.GREEN);
        screen.addField(new ScreenField("NAMEPOS", 4, 31, 20, "", ScreenColor.WHITE, false));

        screen.getBuffer().write(6, 2, "Type options, press Enter.", ScreenColor.TURQUOISE);
        screen.getBuffer().write(7, 4, "2=Renew membership", ScreenColor.TURQUOISE);
        screen.getBuffer().write(7, 26, "5=Display", ScreenColor.TURQUOISE);

        screen.getBuffer().write(9, 2, "Opt", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 6, "Member No", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 20, "Name", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 46, "Tr", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 50, "S", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 54, "Joined", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 64, "Renews", ScreenColor.TURQUOISE);
        screen.getBuffer().write(9, 75, "Whs", ScreenColor.TURQUOISE);

        List<Member> members = memberService.findAll();
        int offset = clampOffset(session.getSubfileOffset(), members.size());
        session.setSubfileOffset(offset);

        LocalDate today = LocalDate.now();
        int row = FIRST_ROW;
        for (int i = offset; i < members.size() && row < FIRST_ROW + PAGE_SIZE; i++, row++) {
            Member member = members.get(i);
            screen.addField(new ScreenField("OPT" + i, row, 2, 2, "", ScreenColor.WHITE, false));
            screen.getBuffer().write(row, 6, member.getMemberNumber(), ScreenColor.GREEN);
            screen.getBuffer().write(row, 20, truncate(member.getName(), 24), ScreenColor.GREEN);
            screen.getBuffer().write(row, 46, member.getTier().getLegacyCode(),
                    member.getTier().earnsAnnualReward() ? ScreenColor.YELLOW : ScreenColor.GREEN);
            screen.getBuffer().write(row, 50, member.isActive() ? "A" : "I",
                    member.isActive() ? ScreenColor.GREEN : ScreenColor.RED);
            screen.getBuffer().write(row, 54, format(member.getJoinDate()), ScreenColor.GREEN);
            screen.getBuffer().write(row, 64, format(member.getRenewalDate()),
                    member.isExpired(today) ? ScreenColor.RED : ScreenColor.GREEN);
            screen.getBuffer().write(row, 75, nullSafe(member.getHomeWarehouse()), ScreenColor.GREEN);
        }

        boolean atBottom = offset + PAGE_SIZE >= members.size();
        screen.getBuffer().writeRight(21, 79, atBottom ? "Bottom" : "More...", ScreenColor.BLUE);

        chrome.drawLegend(screen, "F3=Exit   F5=Refresh   F7=Roll down   F8=Roll up   F12=Cancel");

        String pending = session.consumePendingMessage();
        if (!pending.isEmpty()) {
            if (session.isPendingMessageError()) {
                screen.setError(pending, null);
            } else {
                screen.setInfo(pending);
            }
        }
        chrome.drawMessage(screen);
        return screen;
    }

    @Override
    public void handle(TerminalSession session, TerminalInput input) {
        AidKey key = input.getAidKey();
        List<Member> members = memberService.findAll();

        if (key == AidKey.F8) {
            int next = session.getSubfileOffset() + PAGE_SIZE;
            if (next < members.size()) {
                session.setSubfileOffset(next);
            }
            return;
        }
        if (key == AidKey.F7) {
            session.setSubfileOffset(Math.max(session.getSubfileOffset() - PAGE_SIZE, 0));
            return;
        }
        if (key == AidKey.F5) {
            session.setPendingMessage("Display refreshed.", false);
            return;
        }

        for (int i = 0; i < members.size(); i++) {
            String option = input.get("OPT" + i);
            if (option.isEmpty()) {
                continue;
            }
            Member member = members.get(i);
            if ("2".equals(option)) {
                renew(session, member);
                return;
            }
            if ("5".equals(option)) {
                session.setPendingMessage(describe(member), false);
                return;
            }
            session.setPendingMessage(
                    "Option " + option + " is not valid.  Type a valid option from the list above.",
                    true);
            return;
        }
    }

    private void renew(TerminalSession session, Member member) {
        try {
            Member renewed = memberService.renew(member.getMemberNumber(), LocalDate.now());
            session.setPendingMessage("Membership " + renewed.getMemberNumber()
                    + " renewed through " + format(renewed.getRenewalDate()) + ".", false);
        } catch (RecordNotFoundException ex) {
            session.setPendingMessage(ex.getMessage(), true);
        }
    }

    private String describe(Member member) {
        return member.getMemberNumber() + "  " + member.getTier().getDisplayName()
                + "  fee " + member.getTier().getAnnualFeeUsd() + " USD"
                + (member.getTier().earnsAnnualReward() ? "  2% reward eligible" : "");
    }

    private int clampOffset(int offset, int size) {
        if (offset >= size) {
            return Math.max(((size - 1) / PAGE_SIZE) * PAGE_SIZE, 0);
        }
        return Math.max(offset, 0);
    }

    private String format(LocalDate date) {
        return date == null ? "  /  /  " : date.format(SHORT_DATE);
    }

    private String truncate(String text, int max) {
        String safe = nullSafe(text);
        return safe.length() <= max ? safe : safe.substring(0, max);
    }

    private String nullSafe(String text) {
        return text == null ? "" : text;
    }
}
