package com.costcodemo.wms.core.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.costcodemo.wms.core.domain.Member;
import com.costcodemo.wms.core.domain.MembershipTier;
import com.costcodemo.wms.core.legacy.LegacyDateCodec;
import com.costcodemo.wms.core.legacy.LegacyRecordTranslator;
import com.costcodemo.wms.core.legacy.MbrmastRecord;
import com.costcodemo.wms.core.repository.MbrmastRepository;

/**
 * Member inquiry and renewal against MBRMAST.
 */
@Service
public class MemberService {

    private final MbrmastRepository memberRepository;
    private final LegacyRecordTranslator translator;

    public MemberService(MbrmastRepository memberRepository, LegacyRecordTranslator translator) {
        this.memberRepository = memberRepository;
        this.translator = translator;
    }

    @Transactional(readOnly = true)
    public Optional<Member> findByNumber(String memberNumber) {
        if (memberNumber == null || memberNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return memberRepository.findById(memberNumber.trim()).map(translator::toMember);
    }

    @Transactional(readOnly = true)
    public Member requireByNumber(String memberNumber) {
        return findByNumber(memberNumber).orElseThrow(() -> new RecordNotFoundException(
                "USR0101", "Member number " + memberNumber + " not found."));
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return translateAll(memberRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<Member> findByTier(MembershipTier tier) {
        return translateAll(memberRepository.findByTierCodeOrderByMemberNumber(tier.getLegacyCode()));
    }

    @Transactional(readOnly = true)
    public List<Member> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return findAll();
        }
        return translateAll(memberRepository.searchByName(name.trim()));
    }

    @Transactional(readOnly = true)
    public List<Member> findExpiringOnOrBefore(LocalDate cutoff) {
        Integer encoded = LegacyDateCodec.fromLocalDate(cutoff);
        return translateAll(memberRepository.findExpiringOnOrBefore(encoded));
    }

    /**
     * Renews a membership for twelve months.
     *
     * <p>Renewal runs from the later of today or the current expiry, so renewing early does
     * not forfeit the remaining term — which is how the counter at the warehouse behaves.
     */
    @Transactional
    public Member renew(String memberNumber, LocalDate asOf) {
        MbrmastRecord record = memberRepository.findById(memberNumber.trim())
                .orElseThrow(() -> new RecordNotFoundException(
                        "USR0101", "Member number " + memberNumber + " not found."));

        LocalDate current = LegacyDateCodec.toLocalDate(record.getRenewalDate());
        LocalDate base = (current == null || current.isBefore(asOf)) ? asOf : current;
        LocalDate renewed = base.plusYears(1);

        record.setRenewalDate(LegacyDateCodec.fromLocalDate(renewed));
        record.setStatusFlag("A");
        return translator.toMember(memberRepository.save(record));
    }

    private List<Member> translateAll(List<MbrmastRecord> records) {
        List<Member> members = new ArrayList<>();
        for (MbrmastRecord record : records) {
            members.add(translator.toMember(record));
        }
        return members;
    }
}
