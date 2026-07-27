package com.costcodemo.wms.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.costcodemo.wms.api.dto.MemberResponse;
import com.costcodemo.wms.core.domain.MembershipTier;
import com.costcodemo.wms.core.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Membership endpoints, reading and writing MBRMAST through the core.
 */
@Tag(name = "Members")
@RestController
@RequestMapping("/api/members")
public class MemberRestController {

    private final MemberService memberService;
    private final ApiMapper mapper;

    public MemberRestController(MemberService memberService, ApiMapper mapper) {
        this.memberService = memberService;
        this.mapper = mapper;
    }

    @Operation(summary = "List members, optionally filtered by tier or name")
    @GetMapping
    public List<MemberResponse> list(@RequestParam(required = false) String tier,
                                     @RequestParam(required = false) String name) {
        if (tier != null && !tier.trim().isEmpty()) {
            return mapper.toMemberResponses(
                    memberService.findByTier(MembershipTier.fromLegacyCode(tier)));
        }
        if (name != null && !name.trim().isEmpty()) {
            return mapper.toMemberResponses(memberService.searchByName(name));
        }
        return mapper.toMemberResponses(memberService.findAll());
    }

    @Operation(summary = "Retrieve a single member by member number")
    @GetMapping("/{memberNumber}")
    public MemberResponse get(@PathVariable String memberNumber) {
        return mapper.toResponse(memberService.requireByNumber(memberNumber));
    }

    @Operation(summary = "Renew a membership for twelve months")
    @PostMapping("/{memberNumber}/renew")
    public MemberResponse renew(@PathVariable String memberNumber) {
        return mapper.toResponse(memberService.renew(memberNumber, LocalDate.now()));
    }
}
