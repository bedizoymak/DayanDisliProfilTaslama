package com.dayandisli.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Numerical parity check against src/calculator/utils/weight.parity.test.ts and the geometry
 * math inlined in WeightCalculation.tsx's calculateVolume() (web source,
 * commit 0f71e8c2de417c5ebc9f6eb11c2bc66fa14a7a82).
 */
class WeightTest {

    @Test
    fun `solid cylinder d=50mm l=100mm steel density`() {
        val r = computeWeight(50.0, 100.0, 7.85)
        assertEquals(196349.54084936206, r.volumeMm3, 1e-6)
        assertEquals(196.34954084936206, r.volumeCm3, 1e-9)
        assertEquals(1541.343895667492, r.weightGrams, 1e-9)
        assertEquals(1.541343895667492, r.weightKg, 1e-9)
    }

    @Test
    fun `material densities match the documented table`() {
        assertEquals(7.85, MaterialDensities.STEEL, 0.0)
        assertEquals(8.0, MaterialDensities.STAINLESS_STEEL, 0.0)
        assertEquals(2.7, MaterialDensities.ALUMINUM, 0.0)
        assertEquals(8.5, MaterialDensities.BRASS, 0.0)
        assertEquals(8.8, MaterialDensities.BRONZE, 0.0)
        assertEquals(8.96, MaterialDensities.COPPER, 0.0)
        assertEquals(7.2, MaterialDensities.CAST_IRON, 0.0)
        assertEquals(4.5, MaterialDensities.TITANIUM, 0.0)
    }

    @Test
    fun `pipe geometry volume formula`() {
        // V = (pi/4) * (OD^2 - ID^2) * L, e.g. OD=60, ID=40, L=100
        val outcome = calculateVolumeMm3(GeometryInputs.Pipe(outerDiameter = 60.0, innerDiameter = 40.0, length = 100.0))
        check(outcome is VolumeOutcome.Success)
        val expected = (Math.PI / 4.0) * (60.0 * 60.0 - 40.0 * 40.0) * 100.0
        assertEquals(expected, outcome.volumeMm3, 1e-6)
    }

    @Test
    fun `pipe geometry rejects outer diameter not greater than inner`() {
        val outcome = calculateVolumeMm3(GeometryInputs.Pipe(outerDiameter = 40.0, innerDiameter = 40.0, length = 100.0))
        assertTrue(outcome is VolumeOutcome.PipeDiameterInvalid)
    }

    @Test
    fun `flat bar geometry volume formula`() {
        val outcome = calculateVolumeMm3(GeometryInputs.Flat(width = 50.0, height = 10.0, length = 100.0))
        check(outcome is VolumeOutcome.Success)
        assertEquals(50.0 * 10.0 * 100.0, outcome.volumeMm3, 1e-9)
    }

    @Test
    fun `quantity scales total weight but not unit weight`() {
        val calc = computeWeightCalculation(volumeMm3 = 196349.54084936206, density = 7.85, quantity = 5)
        assertEquals(1.541343895667492, calc.weightKg, 1e-9)
        assertEquals(1.541343895667492 * 5, calc.totalWeightKg, 1e-9)
    }

    @Test
    fun `quantity below 1 is clamped to 1 like Math_max(1, parseInt)`() {
        val calc = computeWeightCalculation(volumeMm3 = 1000.0, density = 7.85, quantity = 0)
        assertEquals(calc.weightKg, calc.totalWeightKg, 0.0)
    }

    @Test
    fun `cost is weight times price per kg times quantity`() {
        val cost = computeCost(weightKg = 1.541343895667492, pricePerKg = 100.0, quantity = 3)
        assertEquals(154.1343895667492, cost.singleCost, 1e-9)
        assertEquals(462.4031687002476, cost.totalCost, 1e-9)
    }
}
