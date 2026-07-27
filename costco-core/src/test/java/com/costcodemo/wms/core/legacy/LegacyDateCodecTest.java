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
            "991231,1999-12-31",
            "1000101,2000-01-01",
            "1260726,2026-07-26",
            "500704,1950-07-04",
            "2001231,2100-12-31"
    })
    void decodesAndReEncodesSymmetrically(int cyymmdd, LocalDate expected) {
        LocalDate decoded = LegacyDateCodec.toLocalDate(cyymmdd);
        assertEquals(expected, decoded);
        assertEquals(Integer.valueOf(LegacyDateCodec.fromLocalDate(expected)),
                LegacyDateCodec.fromLocalDate(decoded));
    }

    @ParameterizedTest
    @CsvSource({
            "1999-12-31,991231",
            "2000-01-01,1000101",
            "1900-01-01,101",
            "2899-12-31,9991231"
    })
    void encodesAndReDecodesSymmetrically(LocalDate date, int expectedCyymmdd) {
        Integer encoded = LegacyDateCodec.fromLocalDate(date);
        assertEquals(Integer.valueOf(expectedCyymmdd), encoded);
        assertEquals(date, LegacyDateCodec.toLocalDate(encoded));
    }

    @Test
    void centuryDigitFlipsBetween1999And2000() {
        LocalDate lastDayOf1999 = LegacyDateCodec.toLocalDate(991231);
        assertEquals(LocalDate.of(1999, 12, 31), lastDayOf1999);
        assertEquals(LocalDate.of(2000, 1, 1), LegacyDateCodec.toLocalDate(1000101));

        // The day after 1999-12-31 carries the century digit, so the encoded value jumps
        // from 0991231 to 1000101 rather than incrementing.
        assertEquals(Integer.valueOf(991231), LegacyDateCodec.fromLocalDate(lastDayOf1999));
        assertEquals(Integer.valueOf(1000101), LegacyDateCodec.fromLocalDate(lastDayOf1999.plusDays(1)));
        assertEquals(1, LegacyDateCodec.fromLocalDate(LocalDate.of(2000, 1, 1)) / 1_000_000);
        assertEquals(0, LegacyDateCodec.fromLocalDate(LocalDate.of(1999, 12, 31)) / 1_000_000);
    }

    @Test
    void nullAndZeroAreTheLegacyNullInBothDirections() {
        assertNull(LegacyDateCodec.toLocalDate(null));
        assertNull(LegacyDateCodec.toLocalDate(0));
        assertEquals(Integer.valueOf(0), LegacyDateCodec.fromLocalDate(null));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -991231, 9_991_232, 1_261_332, 1_260_732})
    void rejectsValuesThatAreNotDecodableDates(int cyymmdd) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> LegacyDateCodec.toLocalDate(cyymmdd));
        assertTrue(ex.getMessage().contains(String.valueOf(cyymmdd)),
                "message should name the offending value but was: " + ex.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"1899-12-31,1899", "2900-01-01,2900"})
    void encodeRejectsYearsOutsideTheSingleCenturyDigitRange(LocalDate date, int year) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> LegacyDateCodec.fromLocalDate(date));
        assertTrue(ex.getMessage().contains(String.valueOf(year)),
                "message should name the offending year but was: " + ex.getMessage());
    }

    @Test
    void formatForDisplayDropsTheCentury() {
        assertEquals("07/26/26", LegacyDateCodec.formatForDisplay(1260726));
        assertEquals("12/31/99", LegacyDateCodec.formatForDisplay(991231));
        assertEquals("01/01/00", LegacyDateCodec.formatForDisplay(1000101));
    }

    /**
     * The 5250 date field is six characters plus separators, so two dates a century apart
     * render identically. This is a property of the fixed-width screen rather than a defect:
     * the century survives in the stored CYYMMDD value and only the display is lossy.
     */
    @Test
    void formatForDisplayCannotDistinguishDatesACenturyApart() {
        assertEquals("12/31/99", LegacyDateCodec.formatForDisplay(991231));
        assertEquals("12/31/99", LegacyDateCodec.formatForDisplay(1991231));
        assertEquals(LocalDate.of(1999, 12, 31), LegacyDateCodec.toLocalDate(991231));
        assertEquals(LocalDate.of(2099, 12, 31), LegacyDateCodec.toLocalDate(1991231));
    }

    @Test
    void formatForDisplayBlanksTheLegacyNull() {
        assertEquals("  /  /  ", LegacyDateCodec.formatForDisplay(0));
        assertEquals("  /  /  ", LegacyDateCodec.formatForDisplay(null));
        assertEquals(8, LegacyDateCodec.formatForDisplay(null).length(),
                "the placeholder must occupy the full width of the screen field");
    }
}
