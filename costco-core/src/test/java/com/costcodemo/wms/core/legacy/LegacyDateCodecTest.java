package com.costcodemo.wms.core.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class LegacyDateCodecTest {

    @Test
    void decodesATwentyFirstCenturyDate() {
        assertEquals(LocalDate.of(2026, 7, 26), LegacyDateCodec.toLocalDate(1260726));
    }

    @Test
    void treatsZeroAsTheLegacyNull() {
        assertNull(LegacyDateCodec.toLocalDate(0));
    }
}
