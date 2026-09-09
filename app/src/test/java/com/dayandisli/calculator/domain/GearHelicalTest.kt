package com.dayandisli.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Numerical parity check against src/calculator/utils/gearHelical.parity.test.ts (web source,
 * commit 0f71e8c2de417c5ebc9f6eb11c2bc66fa14a7a82). Expected values are copied from that file.
 */
class GearHelicalTest {

    @Test
    fun `mn=2 z=25 alpha=20 beta=15 Sag unclamped branch`() {
        val r = computeHelicalParams(2.0, 25, 20.0, 15.0, HelixDirection.SAG)
        assertEquals(0.3490658503988659, r.alphaRad, 1e-12)
        assertEquals(0.2617993877991494, r.betaRad, 1e-12)
        assertEquals(55.76380902050415, r.diameter, 1e-9)
        assertEquals(3.2777777777777777, r.k, 1e-12)
        assertEquals(3, r.kFloor)
        assertEquals(4, r.kCeil)
        assertEquals(0.016453389896665394, r.ew, 1e-12)
        assertEquals(15.533713624108241, r.wkFloor, 1e-9)
        assertEquals(21.43797649229534, r.wkCeil, 1e-9)
        assertEquals(0.64, r.taksimatRatio, 1e-12)
        assertEquals(0.741462, r.helicalGearRatio, 1e-6)
        assertEquals(HelixDirection.SAG, r.helixDirection)
    }

    @Test
    fun `mn=1 z=6 alpha=5 beta=10 Sol clamp branch`() {
        val r = computeHelicalParams(1.0, 6, 5.0, 10.0, HelixDirection.SOL)
        assertEquals(0.08726646259971647, r.alphaRad, 1e-12)
        assertEquals(0.17453292519943295, r.betaRad, 1e-12)
        assertEquals(8.09255967131447, r.diameter, 1e-9)
        assertEquals(0.6666666666666666, r.k, 1e-12)
        // raw kFloor=0, kCeil=1 -> clamped to the documented floors of 2 and 3
        assertEquals(2, r.kFloor)
        assertEquals(3, r.kCeil)
        assertEquals(0.00023261079869554835, r.ew, 1e-12)
        assertEquals(4.695847271471471, r.wkFloor, 1e-9)
        assertEquals(7.8254852165416, r.wkCeil, 1e-9)
        assertEquals(2.6666666666666665, r.taksimatRatio, 1e-12)
        assertEquals(0.994931, r.helicalGearRatio, 1e-6)
        assertEquals(HelixDirection.SOL, r.helixDirection)
    }

    @Test
    fun `taksimatRatio is always 16 over z`() {
        assertEquals(1.0, computeHelicalParams(1.0, 16, 20.0, 10.0, HelixDirection.SAG).taksimatRatio, 1e-12)
    }

    @Test
    fun `accepts the documented valid ranges`() {
        assertTrue(validateHelicalInputs(0.3, 6, 0.0).isEmpty())
        assertTrue(validateHelicalInputs(3.0, 6, 45.0).isEmpty())
    }

    @Test
    fun `rejects helix angle outside 0-45`() {
        assertTrue(validateHelicalInputs(1.0, 20, -0.1).contains("Helis açısı 0 ile 45 derece arasında olmalıdır"))
        assertTrue(validateHelicalInputs(1.0, 20, 45.1).contains("Helis açısı 0 ile 45 derece arasında olmalıdır"))
    }
}
