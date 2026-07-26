package com.costcodemo.wms.core.legacy;

import java.time.LocalDate;

/**
 * Converts between {@link LocalDate} and the 7-digit CYYMMDD numeric dates stored in the
 * DB2/400 physical files.
 *
 * <p>CYYMMDD is the standard IBM i workaround for storing a date in a packed decimal field:
 * a leading century digit (0 for 19xx, 1 for 20xx) followed by YYMMDD. It is why so much
 * RPG carries hand-written date arithmetic, and it is the single most common source of
 * translation bugs when a modern tier reads these files directly.
 *
 * <p>Zero is the legacy null. RPG has no concept of an absent numeric, so an unset date
 * field reads as 0000000 and must not be turned into year zero.
 */
public final class LegacyDateCodec {

    private LegacyDateCodec() {
    }

    /**
     * Decodes a CYYMMDD value. Returns {@code null} for the legacy zero-date sentinel.
     *
     * @throws IllegalArgumentException if the value is not a decodable CYYMMDD date
     */
    public static LocalDate toLocalDate(Integer cyymmdd) {
        if (cyymmdd == null || cyymmdd == 0) {
            return null;
        }
        if (cyymmdd < 0 || cyymmdd > 9_991_231) {
            throw new IllegalArgumentException("Not a CYYMMDD date: " + cyymmdd);
        }

        int century = cyymmdd / 1_000_000;
        int yy = (cyymmdd / 10_000) % 100;
        int month = (cyymmdd / 100) % 100;
        int day = cyymmdd % 100;

        int year = 1900 + (century * 100) + yy;
        try {
            return LocalDate.of(year, month, day);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Not a CYYMMDD date: " + cyymmdd, ex);
        }
    }

    /**
     * Encodes a date as CYYMMDD. A {@code null} date becomes the legacy zero sentinel.
     *
     * @throws IllegalArgumentException if the year falls outside the 1900-2899 range the
     *                                  single century digit can represent
     */
    public static Integer fromLocalDate(LocalDate date) {
        if (date == null) {
            return 0;
        }
        int year = date.getYear();
        if (year < 1900 || year > 2899) {
            throw new IllegalArgumentException("Year outside CYYMMDD range: " + year);
        }
        int century = (year - 1900) / 100;
        int yy = year % 100;
        return (century * 1_000_000) + (yy * 10_000) + (date.getMonthValue() * 100) + date.getDayOfMonth();
    }

    /**
     * Formats a CYYMMDD value for display on a 5250 screen as MM/DD/YY. Terminal date
     * fields are six characters plus separators, so the century is deliberately dropped.
     */
    public static String formatForDisplay(Integer cyymmdd) {
        LocalDate date = toLocalDate(cyymmdd);
        if (date == null) {
            return "  /  /  ";
        }
        return String.format("%02d/%02d/%02d",
                date.getMonthValue(), date.getDayOfMonth(), date.getYear() % 100);
    }
}
