package com.dayandisli.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GearReishauerTest {

    // --- parseReishauerAngle (D,M,S,CS) ---

    @Test
    fun parse_fourComponentAngle_withCentiseconds() {
        val expected = 20.0 + 30.0 / 60.0 + 45.0 / 3600.0 + 25.0 / 360000.0
        assertEquals(expected, parseReishauerAngle("20,30,45,25")!!, 1e-9)
    }

    @Test
    fun parse_zeroCentiseconds_matchesPlainDMS() {
        assertEquals(20.5, parseReishauerAngle("20,30,0,0")!!, 1e-9)
    }

    @Test
    fun parse_tooManyComponents_returnsNull() {
        assertNull(parseReishauerAngle("20,30,45,25,1"))
    }

    @Test
    fun parse_degreesOnly_missingComponentsTreatedAsZero() {
        assertEquals(20.0, parseReishauerAngle("20")!!, 1e-9)
    }

    @Test
    fun parse_degreesAndMinutes_missingComponentsTreatedAsZero() {
        assertEquals(20.5, parseReishauerAngle("20,30")!!, 1e-9)
    }

    @Test
    fun parse_degreesMinutesSeconds_missingCentisecondsTreatedAsZero() {
        val expected = 20.0 + 30.0 / 60.0 + 45.0 / 3600.0
        assertEquals(expected, parseReishauerAngle("20,30,45")!!, 1e-9)
    }

    @Test
    fun parse_emptyMinutesComponent_treatedAsZero() {
        val expected = 20.0 + 45.0 / 3600.0
        assertEquals(expected, parseReishauerAngle("20,,45")!!, 1e-9)
    }

    @Test
    fun parse_emptyString_returnsZero() {
        assertEquals(0.0, parseReishauerAngle("")!!, 1e-9)
    }

    @Test
    fun parse_nonNumericComponent_returnsNull() {
        assertNull(parseReishauerAngle("a,30,45,25"))
    }

    // --- k1/k2/k3 ---

    @Test
    fun compute_kRaw3point8_producesConsecutiveK1K2K3() {
        // alpha * z / 180 + 0.5 = 3.8  =>  alpha*z = 594; alpha=27, z=22 -> 27*22/180=3.3+0.5=3.8
        val mn = 2.5
        val z = 22
        val alpha = 27.0
        val beta = 0.0
        val nominal = computeReishauerNominalDiameter(mn, z, beta)

        val result = computeReishauerParams(mn, z, alpha, "27", beta, "0", HelixDirection.SAG, nominal)

        assertEquals(3, result.k1)
        assertEquals(4, result.k2)
        assertEquals(5, result.k3)
    }

    // --- Wk(k) for all three values ---

    @Test
    fun compute_wkValues_areMonotonicallyIncreasingWithK() {
        val mn = 2.5
        val z = 20
        val alpha = 20.0
        val beta = 15.0
        val nominal = computeReishauerNominalDiameter(mn, z, beta)

        val result = computeReishauerParams(mn, z, alpha, "20", beta, "15", HelixDirection.SAG, nominal)

        assertEquals(result.k1 + 1, result.k2)
        assertEquals(result.k2 + 1, result.k3)
        assertTrue(result.wk1 < result.wk2)
        assertTrue(result.wk2 < result.wk3)
    }

    // --- Helis Çark Oranı ---

    @Test
    fun compute_helixGearRatio_beta20_mn2point5_matchesStatedExample() {
        val mn = 2.5
        val z = 20
        val alpha = 20.0
        val beta = 20.0
        val nominal = computeReishauerNominalDiameter(mn, z, beta)

        val result = computeReishauerParams(mn, z, alpha, "20", beta, "20", HelixDirection.SAG, nominal)

        assertEquals(0.625230820, result.helixGearRatio, 1e-9)
    }

    @Test
    fun compute_helixGearRatio_beta15_mn2point5_matchesStatedExample() {
        val mn = 2.5
        val z = 20
        val alpha = 20.0
        val beta = 15.0
        val nominal = computeReishauerNominalDiameter(mn, z, beta)

        val result = computeReishauerParams(mn, z, alpha, "20", beta, "15", HelixDirection.SAG, nominal)

        assertEquals(0.826220245, result.helixGearRatio, 1e-9)
    }

    @Test
    fun compute_helixGearRatio_betaZero_isZeroSafe() {
        val mn = 2.5
        val z = 20
        val alpha = 20.0
        val beta = 0.0
        val nominal = computeReishauerNominalDiameter(mn, z, beta)

        val result = computeReishauerParams(mn, z, alpha, "20", beta, "0", HelixDirection.SAG, nominal)

        assertEquals(0.0, result.helixGearRatio, 0.0)
    }

    // --- Taksimat Oranı: 16.0 / z ---

    @Test
    fun compute_taksimatOrani_isSixteenOverZ() {
        val mn = 2.5
        val z = 20
        val alpha = 20.0
        val beta = 15.0
        val nominal = computeReishauerNominalDiameter(mn, z, beta)

        val result = computeReishauerParams(mn, z, alpha, "20", beta, "15", HelixDirection.SAG, nominal)

        assertEquals(16.0 / 20.0, result.taksimatRatio, 1e-9)
    }

    // --- Manual diameter correction ---

    @Test
    fun compute_manualDiameterEqualsNominal_producesUnshiftedWk() {
        val mn = 2.5
        val z = 20
        val alpha = 20.0
        val beta = 15.0
        val nominal = computeReishauerNominalDiameter(mn, z, beta)

        val withoutManual = computeReishauerParams(mn, z, alpha, "20", beta, "15", HelixDirection.SAG, nominal)
        val diff = 1.2345
        val withManual = computeReishauerParams(mn, z, alpha, "20", beta, "15", HelixDirection.SAG, nominal + diff)

        val expectedShift = diff * kotlin.math.sin(Math.toRadians(alpha))
        assertEquals(expectedShift, withManual.wk1 - withoutManual.wk1, 0.001)
        assertEquals(expectedShift, withManual.wk2 - withoutManual.wk2, 0.001)
        assertEquals(expectedShift, withManual.wk3 - withoutManual.wk3, 0.001)
    }

    // --- Validation ---

    @Test
    fun validate_allValid_returnsNoErrors() {
        assertTrue(validateReishauerInputs(1.5, 20, 50.0, 20.0, 15.0).isEmpty())
    }

    @Test
    fun validate_moduleOutOfRange_returnsError() {
        assertTrue(validateReishauerInputs(0.1, 20, 50.0, 20.0, 15.0).isNotEmpty())
    }

    @Test
    fun validate_teethBelowMinimum_returnsError() {
        assertTrue(validateReishauerInputs(1.5, 5, 50.0, 20.0, 15.0).isNotEmpty())
    }

    @Test
    fun validate_diameterOutOfRange_returnsError() {
        assertTrue(validateReishauerInputs(1.5, 20, 300.0, 20.0, 15.0).isNotEmpty())
    }

    @Test
    fun validate_unparsedAngle_returnsError() {
        assertTrue(validateReishauerInputs(1.5, 20, 50.0, null, 15.0).isNotEmpty())
        assertTrue(validateReishauerInputs(1.5, 20, 50.0, 20.0, null).isNotEmpty())
    }

    // --- Gear-selection combination search ---

    @Test
    fun reishauerGearList_startsAt25EndsAt127Contains103Gears() {
        assertEquals(25, REISHAUER_GEAR_LIST.first())
        assertEquals(127, REISHAUER_GEAR_LIST.last())
        assertEquals(103, REISHAUER_GEAR_LIST.size)
    }

    @Test
    fun findReishauerTaksimatCombinations_returnsAtMostFiveResults() {
        val results = findReishauerTaksimatCombinations(20)
        assertTrue(results.size <= 5)
        results.forEach { c ->
            assertTrue(c.a in REISHAUER_GEAR_LIST)
            assertTrue(c.b in REISHAUER_GEAR_LIST)
        }
    }

    @Test
    fun findReishauerHelisCombinations_returnsAtMostFiveResults() {
        val results = findReishauerHelisCombinations(0.826220245)
        assertTrue(results.size <= 5)
    }

    @Test
    fun findReishauerCombinations_twoGearPreferredOverFourGearWhenEnoughExist() {
        // z=32 -> target 16/32=0.5, and 25/50 (both within [25,127]) matches exactly, so the
        // 2-gear search should be able to satisfy results without falling back to 4-gear.
        val results = findReishauerTaksimatCombinations(32)
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.c == null && it.d == null })
    }

    // --- Actual helix angle from a combination ratio ---

    @Test
    fun reishauerActualHelixAngle_matchesRequestedBetaForExactRatio() {
        val mn = 2.5
        val beta = 20.0
        val ratio = mn / (11.6909 * kotlin.math.sin(Math.toRadians(beta)))

        val actual = reishauerActualHelixAngle(mn, ratio)

        assertNotNull(actual)
        assertEquals(beta, actual!!, 1e-6)
    }

    @Test
    fun reishauerActualHelixAngle_outOfRangeAsinInput_returnsNull() {
        assertNull(reishauerActualHelixAngle(2.5, 0.01))
    }

    @Test
    fun reishauerActualHelixAngle_zeroRatio_returnsNull() {
        assertNull(reishauerActualHelixAngle(2.5, 0.0))
    }
}
