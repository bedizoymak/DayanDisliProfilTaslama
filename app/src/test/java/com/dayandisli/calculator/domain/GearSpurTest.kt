package com.dayandisli.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Numerical parity check against src/calculator/utils/gearSpur.parity.test.ts (web source,
 * commit 0f71e8c2de417c5ebc9f6eb11c2bc66fa14a7a82). Expected values are copied from that file.
 */
class GearSpurTest {

    @Test
    fun `mn=2 z=20 alpha=20 auto diameter xFactor=0 branch`() {
        val r = computeSpurParams(2.0, 20, 20.0)
        assertEquals(0.3490658503988659, r.alphaRad, 1e-12)
        assertEquals(44.0, r.diameter, 1e-12)
        assertEquals(44.0, r.autoCalculatedDiameter, 1e-12)
        assertEquals(2.7222222222222223, r.k, 1e-12)
        assertEquals(2, r.kFloor)
        assertEquals(3, r.kCeil)
        assertEquals(0.014904383867336446, r.ew, 1e-12)
        assertEquals(0.0, r.xFactor, 0.0)
        assertEquals(9.416615883780512, r.wkFloor, 1e-9)
        assertEquals(15.32087875196761, r.wkCeil, 1e-9)
        assertEquals(0.4, r.taksimatRatio, 1e-12)
    }

    @Test
    fun `mn=1_5 z=30 alpha=14_5 diameter override=50 xFactor active branch`() {
        val r = computeSpurParams(1.5, 30, 14.5, 50.0)
        assertEquals(0.2530727415391778, r.alphaRad, 1e-12)
        assertEquals(50.0, r.diameter, 0.0)
        assertEquals(48.0, r.autoCalculatedDiameter, 1e-12)
        assertEquals(2.9166666666666665, r.k, 1e-12)
        assertEquals(2, r.kFloor)
        assertEquals(3, r.kCeil)
        assertEquals(0.00554484281671247, r.ew, 1e-12)
        assertEquals(0.5007600081088829, r.xFactor, 1e-9)
        assertEquals(7.585762607980779, r.wkFloor, 1e-9)
        assertEquals(12.148050879884012, r.wkCeil, 1e-9)
        assertEquals(0.26666666666666666, r.taksimatRatio, 1e-12)
    }

    @Test
    fun `taksimatRatio is always 8 over z`() {
        assertEquals(0.5, computeSpurParams(1.0, 16, 20.0).taksimatRatio, 1e-12)
        assertEquals(1.0, computeSpurParams(1.0, 8, 20.0).taksimatRatio, 1e-12)
    }

    @Test
    fun `accepts the documented valid ranges`() {
        assertTrue(validateSpurInputs(0.3, 6, 8.0).isEmpty())
        assertTrue(validateSpurInputs(3.0, 6, 250.0).isEmpty())
    }

    @Test
    fun `rejects module outside 0_3-3_0`() {
        assertTrue(validateSpurInputs(0.29, 20, 50.0).contains("Modül 0.3 ile 3.0 arasında olmalıdır"))
        assertTrue(validateSpurInputs(3.01, 20, 50.0).contains("Modül 0.3 ile 3.0 arasında olmalıdır"))
    }

    @Test
    fun `rejects tooth count below 6`() {
        assertTrue(validateSpurInputs(1.0, 5, 50.0).contains("Diş sayısı en az 6 olmalıdır"))
    }

    @Test
    fun `rejects diameter outside 8-250`() {
        assertTrue(validateSpurInputs(1.0, 20, 7.9).contains("Çap 8 ile 250 mm arasında olmalıdır"))
        assertTrue(validateSpurInputs(1.0, 20, 250.1).contains("Çap 8 ile 250 mm arasında olmalıdır"))
    }
}
