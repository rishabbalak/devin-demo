package com.costcodemo.wms.core.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class LegacyDateCodecTest {

    @Test
    void decodesATwentyFirstCenturyDate() {
        assertEquals(LocalDate.of(2026, 7, 26), LegacyDateCodec.toLocalDate(1260726));
    }

    @Test
    void treatsZeroAsTheLegacyNull() {
        assertNull(LegacyDateCodec.toLocalDate(0));
    }

    @ParameterizedTest
    @CsvSource({
            "1900-01-01",
            "1999-12-31",
            "2000-01-01",
            "2026-07-26",
            "2899-12-31"
    })
    void roundTripsLocalDateThroughCyymmdd(LocalDate date) {
        assertEquals(date, LegacyDateCodec.toLocalDate(LegacyDateCodec.fromLocalDate(date)));
    }

    @ParameterizedTest
    @ValueSource(ints = {101, 991231, 1000101, 1260726, 9991231})
    void roundTripsCyymmddThroughLocalDate(int cyymmdd) {
        assertEquals(cyymmdd, LegacyDateCodec.fromLocalDate(LegacyDateCodec.toLocalDate(cyymmdd)));
    }

    @Test
    void encodesTheLastDayOf1999WithCenturyDigitZero() {
        assertEquals(991231, LegacyDateCodec.fromLocalDate(LocalDate.of(1999, 12, 31)));
    }

    @Test
    void encodesTheFirstDayOf2000WithCenturyDigitOne() {
        assertEquals(1000101, LegacyDateCodec.fromLocalDate(LocalDate.of(2000, 1, 1)));
    }

    @Test
    void decodesEitherSideOfTheCenturyBoundaryAsConsecutiveDays() {
        LocalDate endOf1999 = LegacyDateCodec.toLocalDate(991231);
        LocalDate startOf2000 = LegacyDateCodec.toLocalDate(1000101);

        assertEquals(LocalDate.of(1999, 12, 31), endOf1999);
        assertEquals(LocalDate.of(2000, 1, 1), startOf2000);
        assertEquals(startOf2000, endOf1999.plusDays(1));
    }

    @Test
    void decodesANullInputToNull() {
        assertNull(LegacyDateCodec.toLocalDate(null));
    }

    @Test
    void encodesANullDateAsExactlyZero() {
        assertEquals(0, LegacyDateCodec.fromLocalDate(null));
    }

    @Test
    void roundTripsTheLegacyNullSentinelInBothDirections() {
        assertNull(LegacyDateCodec.toLocalDate(LegacyDateCodec.fromLocalDate(null)));
        assertEquals(0, LegacyDateCodec.fromLocalDate(LegacyDateCodec.toLocalDate(0)));
    }

    @ParameterizedTest
    @CsvSource({
            "1899-12-31",
            "2900-01-01"
    })
    void rejectsYearsOutsideTheSingleCenturyDigitRange(LocalDate date) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> LegacyDateCodec.fromLocalDate(date));
        assertTrue(ex.getMessage().contains(String.valueOf(date.getYear())), ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 1261332, 1260732, 9991232})
    void rejectsValuesThatAreNotDecodableDates(int cyymmdd) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> LegacyDateCodec.toLocalDate(cyymmdd));
        assertTrue(ex.getMessage().contains(String.valueOf(cyymmdd)), ex.getMessage());
    }
}
