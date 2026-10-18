package com.ontadev.kits.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeFormatTest {

    @Test
    void formatsAllParts() {
        assertEquals("1д 2ч 3м 4с", TimeFormat.format(86400 + 7200 + 180 + 4, "д", "ч", "м", "с"));
    }

    @Test
    void skipsZeroParts() {
        assertEquals("1ч 5с", TimeFormat.format(3605, "д", "ч", "м", "с"));
    }

    @Test
    void zeroIsZeroSeconds() {
        assertEquals("0с", TimeFormat.format(0, "д", "ч", "м", "с"));
    }

    @Test
    void parsesPlainSeconds() {
        assertEquals(3600, TimeFormat.parse("3600"));
        assertEquals(0, TimeFormat.parse("0"));
    }

    @Test
    void parsesUnits() {
        assertEquals(86400 + 2 * 3600 + 30 * 60, TimeFormat.parse("1d 2h 30m"));
        assertEquals(90, TimeFormat.parse("1м30с"));
        assertEquals(45, TimeFormat.parse("45s"));
    }

    @Test
    void rejectsGarbage() {
        assertEquals(-1, TimeFormat.parse(""));
        assertEquals(-1, TimeFormat.parse("abc"));
        assertEquals(-1, TimeFormat.parse("5x"));
        assertEquals(-1, TimeFormat.parse("1h foo"));
        assertEquals(-1, TimeFormat.parse(null));
    }
}
