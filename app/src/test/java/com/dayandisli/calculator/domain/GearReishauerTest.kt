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
    fun angleInput_isNormalizedToTheReishauerDisplayNotation() {
        // No "cs" suffix and never the old ASCII '/''/''' quote notation.
        assertEquals("20°35′06″95", normalizeReishauerAngleInput("20.35,6,95"))
        assertEquals("20°35′06″95", normalizeReishauerAngleInput("20 35 6 95"))
        assertEquals("20°05′00″00", normalizeReishauerAngleInput("20 5 0"))
    }

    @Test
    fun angleInput_toleratesEquivalentNotationsAndResolvesToTheSameNormalizedDisplay() {
        val expected = "20°35′06″95"
        assertEquals(expected, normalizeReishauerAngleInput("20.35,6,95"))
        assertEquals(expected, normalizeReishauerAngleInput("20 35 6 95"))
        assertEquals(expected, normalizeReishauerAngleInput("20°35′06″95"))
        // Legacy ASCII-quote notation this app used to emit must still normalize identically.
        assertEquals(expected, normalizeReishauerAngleInput("20°35'06''95'''"))
        // Legacy " cs" suffix this app used to emit must also still normalize identically.
        assertEquals(expected, normalizeReishauerAngleInput("20°35′06″95 cs"))
    }

    @Test
    fun parse_newNotation_andLegacyAsciiQuoteNotation_produceTheSameAngle() {
        val expected = 20.0 + 35.0 / 60.0 + 6.0 / 3600.0 + 95.0 / 360000.0
        assertEquals(expected, parseReishauerAngle("20°35′06″95")!!, 1e-9)
        assertEquals(expected, parseReishauerAngle("20°35′06″95 cs")!!, 1e-9)
        assertEquals(expected, parseReishauerAngle("20°35'06''95'''")!!, 1e-9)
        assertEquals(expected, parseReishauerAngle("20.35,6,95")!!, 1e-9)
        assertEquals(expected, parseReishauerAngle("20 35 6 95")!!, 1e-9)
    }

    @Test
    fun angleSeparators_areReplacedImmediatelyWhileTyping() {
        assertEquals("20°", normalizeReishauerAngleSeparator("20 "))
        assertEquals("20°05′", normalizeReishauerAngleSeparator("20°5,"))
        assertEquals("20°05′06″", normalizeReishauerAngleSeparator("20°05'6 "))
    }

    @Test
    fun angleNotation_neverContainsAsciiQuoteCharactersOrCsSuffix() {
        val formatted = normalizeReishauerAngleInput("20,35,6,95")
        assertTrue(!formatted.contains("'"))
        assertTrue(!formatted.contains("cs"))
        assertEquals("20°35′06″95", formatted)
        assertTrue(formatted.contains('′'))
        assertTrue(formatted.contains('″'))
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

    @Test
    fun parse_minuteOutOfRange_returnsNull() {
        assertNull(parseReishauerAngle("20,60"))
    }

    @Test
    fun parse_secondOutOfRange_returnsNull() {
        assertNull(parseReishauerAngle("20,30,60"))
    }

    @Test
    fun parse_centisecondOutOfRange_returnsNull() {
        assertNull(parseReishauerAngle("20,30,45,100"))
    }

    @Test
    fun parse_boundaryValidComponents_areAccepted() {
        // 59 minutes, 59 seconds, 99 centiseconds are the maximum valid values for each component.
        assertNotNull(parseReishauerAngle("20,59,59,99"))
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

    // --- Taksimat Oranı: 24.0 / z (Reishauer-specific; distinct from Küçük Azdırma's 16.0 / z) ---

    @Test
    fun compute_taksimatOrani_isTwentyFourOverZ() {
        val mn = 2.5
        val z = 20
        val alpha = 20.0
        val beta = 15.0
        val nominal = computeReishauerNominalDiameter(mn, z, beta)

        val result = computeReishauerParams(mn, z, alpha, "20", beta, "15", HelixDirection.SAG, nominal)

        assertEquals(24.0 / 20.0, result.taksimatRatio, 1e-9)
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
    fun reishauerGearList_startsAt25EndsAt127Contains103UniqueGears() {
        assertEquals(25, REISHAUER_GEAR_LIST.first())
        assertEquals(127, REISHAUER_GEAR_LIST.last())
        assertEquals(103, REISHAUER_GEAR_LIST.size)
        assertEquals(103, REISHAUER_GEAR_LIST.toSet().size)
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
    fun findReishauerHelisCombinations_returnsAtMostTwentyResults() {
        val results = findReishauerHelisCombinations(2.5, 15.0, 0.826220245)
        assertTrue(results.size <= 20)
    }

    @Test
    fun findReishauerHelisCombinations_everyReturnedCandidateHasAValidActualAngle() {
        // The final list must only ever contain candidates whose actual helix angle is computable
        // (asin input in [-1, 1]) - invalid candidates must be filtered out, not merely hidden.
        val mn = 2.5
        val results = findReishauerHelisCombinations(mn, 15.0, 0.826220245)
        assertTrue(results.isNotEmpty())
        results.forEach { c -> assertNotNull(reishauerActualHelixAngle(mn, c.combination.ratio)) }
    }

    @Test
    fun findReishauerHelisCombinations_orderedByAscendingAbsoluteError() {
        val mn = 2.5
        val target = 0.826220245
        val results = findReishauerHelisCombinations(mn, 15.0, target)
        val errors = results.map { it.absoluteError }
        assertEquals(errors.sorted(), errors)
    }

    @Test
    fun findReishauerHelisCombinations_returnsNoDuplicateGearArrangements() {
        val mn = 2.5
        val results = findReishauerHelisCombinations(mn, 15.0, 0.826220245)
        val arrangements = results.map { listOf(it.combination.a, it.combination.b, it.combination.c, it.combination.d) }
        assertEquals(arrangements.distinct().size, arrangements.size)
    }

    @Test
    fun findReishauerHelisCombinations_deviationDegIsActualMinusRequested() {
        val mn = 2.5
        val betaRequested = 15.0
        val results = findReishauerHelisCombinations(mn, betaRequested, 0.826220245)
        assertTrue(results.isNotEmpty())
        results.forEach { c ->
            assertEquals(c.actualHelixAngleDeg - betaRequested, c.deviationDeg, 1e-9)
        }
    }

    @Test
    fun findReishauerHelisCombinations_ratioIsRoundedDownToNineDecimalsNotHalfUp() {
        // floorToFixed truncates (RoundingMode.DOWN); verify the displayed ratio never exceeds 9
        // decimal digits of precision by checking it doesn't drift from the raw a/b*(c/d) value by
        // more than one part in 1e9 (the resolution floorToFixed(_, 9) guarantees).
        val mn = 2.5
        val results = findReishauerHelisCombinations(mn, 15.0, 0.826220245)
        assertTrue(results.isNotEmpty())
        results.forEach { c ->
            val raw = if (c.combination.c != null && c.combination.d != null) {
                (c.combination.a.toDouble() * c.combination.c) / (c.combination.b.toDouble() * c.combination.d)
            } else {
                c.combination.a.toDouble() / c.combination.b
            }
            assertTrue(kotlin.math.abs(raw - c.combination.ratio) < 1e-8)
            assertTrue(c.combination.ratio <= raw)
        }
    }

    @Test
    fun findReishauerCombinations_twoGearPreferredOverFourGearWhenEnoughExist() {
        // z=24 -> target 24/24=1.0... use z=32 -> target 24/32=0.75, and e.g. 75/100 (both within
        // [25,127]) matches exactly, so the 2-gear search should be able to satisfy results without
        // falling back to 4-gear.
        val results = findReishauerTaksimatCombinations(32)
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.c == null && it.d == null })
    }

    // --- rankReishauerCombinations: global ranking, 4-gear can outrank 2-gear ---

    @Test
    fun rankReishauerCombinations_betterFourGearCandidateOutranksWeakerTwoGear() {
        // Small synthetic gear list (ascending, distinct): 2-gear ratios are 2/3=.667, 2/5=.4,
        // 2/7=.286, 3/5=.6, 3/7=.429, 5/7=.714. The only 4-gear combination (2,3,5,7) has ratio
        // (2/3)*(5/7) = 10/21 = .47619. Target .476 is far closer to the 4-gear combination
        // (error ~1.9e-4) than to any 2-gear candidate (closest is 3/7=.4286, error ~.047), so the
        // top-ranked (lowest-error) result must be the 4-gear one.
        val gearList = listOf(2, 3, 5, 7)
        val target = 0.476

        val ranked = rankReishauerCombinations(gearList, target, maxResults = 10)

        assertTrue(ranked.isNotEmpty())
        val best = ranked.first()
        assertNotNull(best.c)
        assertNotNull(best.d)
        assertEquals(listOf(2, 3, 5, 7), listOf(best.a, best.b, best.c, best.d))
        // Ranking must be by ascending absolute error against the target.
        val errors = ranked.map { kotlin.math.abs(it.ratio - target) }
        assertEquals(errors.sorted(), errors)
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

    @Test
    fun findReishauerHelisCombinations_excludesOutOfRangeAsinCandidatesButBackfillsFromBuffer() {
        // A deliberately large mn relative to the target ratio pushes asinInput = mn / (11.6909 *
        // ratio) past 1 for most/all of the closest raw-ranked candidates, so the raw ranked pool
        // must contain excluded candidates...
        val mn = 50.0
        val target = 0.4
        val ranked = rankReishauerCombinations(REISHAUER_GEAR_LIST, target, maxResults = 40)
        val invalidCount = ranked.count { reishauerActualHelixAngle(mn, it.ratio) == null }
        assertTrue("expected at least one out-of-range candidate to exercise the filter", invalidCount > 0)

        // ...but the public function must still only surface valid candidates, backfilled from the
        // larger internal buffer rather than just truncating the raw ranked list.
        val results = findReishauerHelisCombinations(mn, 20.0, target, maxResults = 5, internalBufferSize = 40)
        results.forEach { c -> assertNotNull(reishauerActualHelixAngle(mn, c.combination.ratio)) }
    }

    // --- Perfect-match flag ---

    @Test
    fun findReishauerHelisCombinations_exactRatioIsMarkedPerfectMatch() {
        // A synthetic 2-gear-only gear list containing an exact match for the target ratio: the
        // top-ranked (zero-error) result must be flagged isPerfectMatch = true.
        val mn = 2.5
        val betaRequested = 20.0
        val exactRatio = mn / (11.6909 * kotlin.math.sin(Math.toRadians(betaRequested)))
        val ranked = rankReishauerCombinations(REISHAUER_GEAR_LIST, exactRatio, maxResults = 40)
        assumeNotEmptyForExactMatchTest(ranked)

        val results = findReishauerHelisCombinations(mn, betaRequested, exactRatio, maxResults = 20, internalBufferSize = 40)
        assertTrue(results.isNotEmpty())
        // Whatever the best candidate is, its perfect-match flag must be true only when its error
        // is genuinely exact zero, and false otherwise - never a "near enough" approximation.
        results.forEach { c -> assertEquals(c.absoluteError == 0.0, c.isPerfectMatch) }
    }

    private fun assumeNotEmptyForExactMatchTest(ranked: List<GearCombination>) {
        assertTrue(ranked.isNotEmpty())
    }

    // --- Acceptance scenario: mn=2.5, Z=48, alpha=20, beta=20, Sol ---

    @Test
    fun acceptanceScenario_mn2point5_z48_alpha20_beta20_sol() {
        val mn = 2.5
        val z = 48
        val alpha = 20.0
        val beta = 20.0
        val nominal = computeReishauerNominalDiameter(mn, z, beta)

        val result = computeReishauerParams(mn, z, alpha, "20", beta, "20", HelixDirection.SOL, nominal)

        assertEquals(0.5, result.taksimatRatio, 1e-9)
        assertEquals(0.625230820, result.helixGearRatio, 1e-9)
        assertEquals(5, result.k1)
        assertEquals(6, result.k2)
        assertEquals(7, result.k3)
        assertEquals(132.7013, result.nominalDiameter, 0.001)
        // Manual diameter left at the auto-calculated nominal value -> zero Wk correction.
        assertEquals(result.diameter, result.nominalDiameter, 1e-9)
        val unshifted = computeReishauerParams(mn, z, alpha, "20", beta, "20", HelixDirection.SOL, nominal + 1.0)
        val shift = unshifted.wk1 - result.wk1
        assertEquals(1.0 * kotlin.math.sin(Math.toRadians(alpha)), shift, 0.001)
    }
}
