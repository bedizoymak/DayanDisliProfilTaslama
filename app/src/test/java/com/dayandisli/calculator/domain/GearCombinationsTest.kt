package com.dayandisli.calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Numerical parity check against src/calculator/utils/gearCombinations.parity.test.ts (web
 * source, commit 0f71e8c2de417c5ebc9f6eb11c2bc66fa14a7a82).
 */
class GearCombinationsTest {

    @Test
    fun `gear inventories are the exact restored lists`() {
        assertEquals(
            listOf(24, 25, 30, 30, 32, 33, 34, 36, 40, 41, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62,
                63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 75, 76, 80, 85, 90, 90, 94, 96),
            GEAR_LIST_TAKSIMAT,
        )
        assertEquals(
            listOf(22, 23, 23, 24, 25, 28, 30, 32, 35, 36, 38, 40, 42, 42, 44, 45, 46, 47, 47, 49, 50, 52, 53,
                54, 55, 58, 60, 69, 75, 80, 89),
            GEAR_LIST_HELICAL,
        )
    }

    private fun combinationRatio(c: GearCombination): Double =
        if (c.c != null && c.d != null) (c.a.toDouble() * c.c) / (c.b.toDouble() * c.d) else c.a.toDouble() / c.b

    @Test
    fun `findSpurCombinations z=20 target 0_4 exact two-gear matches padded to maxResults`() {
        val combos = findSpurCombinations(20)
        val twoGear = combos.filter { it.c == null && it.d == null }
        val pairs = twoGear.map { it.a to it.b }
        assertEquals(listOf(24 to 60, 30 to 75, 32 to 80, 34 to 85, 36 to 90), pairs)
        assertEquals(10, combos.size)
        combos.forEach { assertEquals(0.4, combinationRatio(it), 1e-9) }
    }

    @Test
    fun `findTaksimatCombinations z=16 target 1_0 rejects trivial same-tooth-count pairs`() {
        // Target ratio is exactly 1.0 here, which is only reachable via a trivial A==B pair (e.g.
        // the duplicate 30/30, 90/90 entries in GEAR_LIST_TAKSIMAT) or a trivial 1:1 four-gear
        // train — both are rejected as non-informative combinations, so no results are returned.
        val combos = findTaksimatCombinations(16)
        assertTrue(combos.none { it.a == it.b })
        assertTrue(combos.isEmpty())
    }

    @Test
    fun `isValidTwoGear rejects equal tooth counts and trivial ratio`() {
        assertTrue(isValidTwoGear(23, 23, 1.0).not())
        assertTrue(isValidTwoGear(30, 30, 1.0).not())
        assertTrue(isValidTwoGear(23, 24, 23.0 / 24.0))
    }

    @Test
    fun `isValidFourGear rejects duplicate values in any position including cross-paired`() {
        // 23/23 x 89/60 - same-stage duplicate (A==B).
        assertTrue(isValidFourGear(23, 23, 89, 60, (23.0 * 89) / (23.0 * 60)).not())
        // 23/60 x 89/23 - cross-paired duplicate (A==D), not caught by a same-stage check alone.
        assertTrue(isValidFourGear(23, 60, 89, 23, (23.0 * 89) / (60.0 * 23)).not())
        // A==C, B==D, B==C, A==B duplicate positions.
        assertTrue(isValidFourGear(23, 60, 23, 89, (23.0 * 23) / (60.0 * 89)).not())
        assertTrue(isValidFourGear(23, 60, 89, 60, (23.0 * 89) / (60.0 * 60)).not())
        assertTrue(isValidFourGear(23, 60, 60, 89, (23.0 * 60) / (60.0 * 89)).not())
        // A distinct four-gear train with no repeated tooth counts remains valid.
        assertTrue(isValidFourGear(22, 30, 89, 44, (22.0 * 89) / (30.0 * 44)))
    }

    @Test
    fun `findHelicalCombinations never contains a trivial or duplicate-tooth-count combination`() {
        // Sweep a range of targets, including ones close to where 23/23-style trivial matches
        // could previously slip through, and assert every returned combination is non-trivial.
        val targets = listOf(0.741462, 1.482924, 1.0, 0.826220245, 0.625230820)
        for (target in targets) {
            val combos = findHelicalCombinations(target)
            combos.forEach { c ->
                assertTrue("2-gear combo must not have equal tooth counts: $c", c.a != c.b)
                val cc = c.c
                val dd = c.d
                if (cc != null && dd != null) {
                    assertTrue(
                        "4-gear combo must have four distinct tooth counts: $c",
                        setOf(c.a, c.b, cc, dd).size == 4,
                    )
                }
            }
        }
    }

    @Test
    fun `findHelicalCombinations still returns valid four-distinct-gear combinations`() {
        val combos = findHelicalCombinations(1.482924)
        val fourGear = combos.filter { it.c != null && it.d != null }
        assertTrue(fourGear.isNotEmpty())
        fourGear.forEach { c -> assertEquals(4, setOf(c.a, c.b, c.c, c.d).size) }
    }

    @Test
    fun `findHelicalCombinations results are all within tolerance of target`() {
        val target = 0.741462
        val combos = findHelicalCombinations(target)
        assertTrue(combos.isNotEmpty())
        combos.forEach { c ->
            val ratio = if (c.c != null && c.d != null) (c.a.toDouble() * c.c) / (c.b.toDouble() * c.d) else c.a.toDouble() / c.b
            assertTrue(abs(ratio - target) <= 0.001)
        }
    }
}
