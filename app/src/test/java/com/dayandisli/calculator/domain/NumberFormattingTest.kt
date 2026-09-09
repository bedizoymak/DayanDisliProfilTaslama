package com.dayandisli.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `parseLocaleInt parses plain integers and comma-decimal fallback`() {
        assertEquals(20, parseLocaleInt("20"))
        assertEquals(6, parseLocaleInt("6,0"))
    }
}
