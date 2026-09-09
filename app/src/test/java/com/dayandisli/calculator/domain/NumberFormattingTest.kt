package com.dayandisli.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberFormattingTest {

    @Test
    fun `toFixedJs rounds like JS toFixed for typical values`() {
        assertEquals("9.4166", 9.416615883780512.toFixedJs(4))
        assertEquals("0.400000", 0.4.toFixedJs(6))
        assertEquals("44.00", 44.0.toFixedJs(2))
    }

    @Test
    fun `parseLocaleDouble treats comma as the Turkish decimal separator`() {
        assertEquals(1.5, parseLocaleDouble("1,5"))
        assertEquals(1.5, parseLocaleDouble("1.5"))
        assertEquals(0.3, parseLocaleDouble("0,3"))
    }

    @Test
    fun `parseLocaleDouble rejects blank input`() {
        assertNull(parseLocaleDouble(""))
        assertNull(parseLocaleDouble("   "))
    }

    @Test
    fun `normalizeDecimalInput accepts spaced decimal module notation`() {
        assertEquals("2.5", normalizeDecimalInput("2 5"))
        assertEquals("2.5", normalizeDecimalInput("2.5"))
        assertEquals("2.5", normalizeDecimalInput("2 . 5"))
        assertEquals("2.5", normalizeDecimalInput("2,5"))
        assertEquals("2.6", normalizeDecimalInput("2..6"))
        assertEquals("2.6", normalizeDecimalInput("2,,6"))
        assertEquals("2.6", normalizeDecimalInput("2 . . 6"))
    }

    @Test
    fun `normalizeIntegerInput removes accidental separators without creating a decimal`() {
        assertEquals("26", normalizeIntegerInput("2 6"))
        assertEquals("26", normalizeIntegerInput("2..6"))
        assertEquals("26", normalizeIntegerInput("2,6"))
    }

    @Test
    fun `parseLocaleInt parses plain integers and comma-decimal fallback`() {
        assertEquals(20, parseLocaleInt("20"))
        assertEquals(6, parseLocaleInt("6,0"))
    }

    // --- DMSCS (D° M′ S″ CS) formatting ---

    @Test
    fun `toDmscs zero-pads minutes seconds and centiseconds to two digits`() {
        assertEquals("19° 59′ 59″ 95", 19.99998611.toDmscs())
        assertEquals("0° 00′ 00″ 15", (0.00 + 15.0 / 360000.0).toDmscs())
    }

    @Test
    fun `toDmscs carries centiseconds into seconds`() {
        // 59.996 centiseconds of a second rounds to 100 -> must carry, never display "60 CS".
        val value = 1.0 / 3600.0 * 0.9999 // just under 1 second
        val formatted = value.toDmscs()
        assertTrue(!formatted.contains("100"))
    }

    @Test
    fun `toDmscs carries seconds into minutes and minutes into degrees`() {
        // 59 minutes 59.999 seconds must carry into the next whole degree, never showing "60".
        val value = 1.0 - 0.0000001
        val formatted = value.toDmscs()
        assertEquals("1° 00′ 00″ 00", formatted)
    }

    @Test
    fun `toDmscs never shows a sign`() {
        assertEquals("0° 00′ 00″ 15", (-15.0 / 360000.0).toDmscs())
    }

    @Test
    fun `toDmscsSigned always shows a leading sign`() {
        assertEquals("+0° 00′ 00″ 15", (15.0 / 360000.0).toDmscsSigned())
        assertEquals("-0° 00′ 00″ 15", (-15.0 / 360000.0).toDmscsSigned())
    }
}
