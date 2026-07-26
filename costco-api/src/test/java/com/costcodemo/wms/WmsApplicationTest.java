package com.costcodemo.wms;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.costcodemo.wms.core.service.MemberService;

@SpringBootTest
class WmsApplicationTest {

    @Autowired
    private MemberService memberService;

    @Test
    void contextLoadsAndTheMemberFileIsSeeded() {
        assertFalse(memberService.findAll().isEmpty());
    }
}
