package com.costcodemo.wms.core.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void decodesATwentiethCenturyDate() {
        assertEquals(LocalDate.of(1926, 7, 26), LegacyDateCodec.toLocalDate(260726));
    }

    @Test
    void treatsZeroAsTheLegacyNull() {
        assertNull(LegacyDateCodec.toLocalDate(0));
    }

    @Test
    void treatsNullAsTheLegacyNull() {
        assertNull(LegacyDateCodec.toLocalDate(null));
    }

    @Test
    void encodesNullAsTheZeroSentinel() {
        assertEquals(0, LegacyDateCodec.fromLocalDate(null));
    }

    @ParameterizedTest
    @CsvSource({
            "1900-01-01, 101",
            "1926-07-26, 260726",
            "1999-12-31, 991231",
            "2000-01-01, 1000101",
            "2026-07-26, 1260726",
            "2899-12-31, 9991231"
    })
    void encodesAndDecodesTheSameCalendarDay(LocalDate date, int cyymmdd) {
        assertEquals(cyymmdd, LegacyDateCodec.fromLocalDate(date));
        assertEquals(date, LegacyDateCodec.toLocalDate(cyymmdd));
        assertEquals(date, LegacyDateCodec.toLocalDate(LegacyDateCodec.fromLocalDate(date)));
        assertEquals(cyymmdd, LegacyDateCodec.fromLocalDate(LegacyDateCodec.toLocalDate(cyymmdd)));
    }

    @Test
    void rollsTheCenturyDigitOverBetween1999And2000() {
        LocalDate lastDayOf1999 = LocalDate.of(1999, 12, 31);
        LocalDate firstDayOf2000 = lastDayOf1999.plusDays(1);

        assertEquals(991231, LegacyDateCodec.fromLocalDate(lastDayOf1999));
        assertEquals(1000101, LegacyDateCodec.fromLocalDate(firstDayOf2000));
        assertEquals(lastDayOf1999, LegacyDateCodec.toLocalDate(991231));
        assertEquals(firstDayOf2000, LegacyDateCodec.toLocalDate(1000101));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -1260726, 9_991_232, 10_000_000, 1261301, 1260230, 1260700, 1260032})
    void rejectsValuesThatAreNotCyymmddDates(int cyymmdd) {
        assertThrows(IllegalArgumentException.class, () -> LegacyDateCodec.toLocalDate(cyymmdd));
    }

    @ParameterizedTest
    @CsvSource({"1899-12-31", "2900-01-01"})
    void rejectsYearsTheSingleCenturyDigitCannotRepresent(LocalDate date) {
        assertThrows(IllegalArgumentException.class, () -> LegacyDateCodec.fromLocalDate(date));
    }

    @Test
    void formatsADateForTheTerminalAsMmDdYy() {
        assertEquals("07/26/26", LegacyDateCodec.formatForDisplay(1260726));
        assertEquals("01/02/00", LegacyDateCodec.formatForDisplay(1000102));
    }

    @Test
    void formatsTheLegacyNullAsTheBlankScreenPlaceholder() {
        assertEquals("  /  /  ", LegacyDateCodec.formatForDisplay(0));
        assertEquals("  /  /  ", LegacyDateCodec.formatForDisplay(null));
    }

    /**
     * The display format is six digits plus separators because that is the width of the
     * date field on the 5250 screen, so the century digit is dropped and 1999 and 2099
     * are indistinguishable to the operator. This is a property of the fixed-width screen,
     * not a bug to fix: widening the display would change every DDS record format.
     */
    @Test
    void dropsTheCenturyDigitSoTwoCenturiesShareOneDisplay() {
        String nineteen = LegacyDateCodec.formatForDisplay(LegacyDateCodec.fromLocalDate(LocalDate.of(1999, 12, 31)));
        String twenty = LegacyDateCodec.formatForDisplay(LegacyDateCodec.fromLocalDate(LocalDate.of(2099, 12, 31)));

        assertEquals("12/31/99", nineteen);
        assertEquals("12/31/99", twenty);
    }
}
