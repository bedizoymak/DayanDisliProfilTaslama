package com.dayandisli.calculator.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Ported from src/calculator/utils/gearCombinations.ts. Iteration order, dedup keys and result
 * limits are reproduced exactly since the receipt tables display combinations in this order.
 */
data class GearCombination(
    val a: Int,
    val b: Int,
    val c: Int?,
    val d: Int?,
    val ratio: Double,
)

val GEAR_LIST_TAKSIMAT: List<Int> = listOf(
    24, 25, 30, 30, 32, 33, 34, 36, 40, 41, 50, 51, 52, 53, 54,
    55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
    70, 71, 72, 75, 76, 80, 85, 90, 90, 94, 96,
)

val GEAR_LIST_HELICAL: List<Int> = listOf(
    22, 23, 23, 24, 25, 28, 30, 32, 35, 36, 38, 40, 42, 42, 44, 45,
    46, 47, 47, 49, 50, 52, 53, 54, 55, 58, 60, 69, 75, 80, 89,
)

/** A tooth-count ratio is considered trivially 1:1 (and therefore not a real gear reduction) when
 * it is within this epsilon of 1.0. */
private const val TRIVIAL_RATIO_EPSILON = 1e-9

private fun isTrivialRatio(value: Double): Boolean = abs(value - 1.0) <= TRIVIAL_RATIO_EPSILON

/** Rejects a 2-gear candidate whose tooth counts are equal (by value, not list position/index —
 * source gear lists contain duplicate values like the two separate `23` entries above, so index
 * inequality alone does not guarantee a non-trivial combination) or whose ratio is effectively 1. */
internal fun isValidTwoGear(a: Int, b: Int, ratio: Double): Boolean {
    if (a == b) return false
    if (isTrivialRatio(ratio)) return false
    return true
}

/** Rejects a 4-gear candidate when any two of its tooth-count values are equal, when either stage
 * (A/B or C/D) is effectively a 1:1 ratio, or when the full train (A/B)*(C/D) is effectively 1:1 —
 * including cross-paired duplicates like A=23,D=23 (e.g. 23/60 x 89/23), not just same-stage ones. */
internal fun isValidFourGear(a: Int, b: Int, c: Int, d: Int, ratio: Double): Boolean {
    if (a == b || a == c || a == d || b == c || b == d || c == d) return false
    if (isTrivialRatio(a.toDouble() / b.toDouble())) return false
    if (isTrivialRatio(c.toDouble() / d.toDouble())) return false
    if (isTrivialRatio(ratio)) return false
    return true
}

private fun findTwoGearCombinations(
    targetRatio: Double,
    gearList: List<Int>,
    tolerance: Double,
): List<GearCombination> {
    val combinations = mutableListOf<GearCombination>()
    val seen = HashSet<String>()

    for (i in gearList.indices) {
        for (j in gearList.indices) {
            if (i == j) continue

            val a = gearList[i]
            val b = gearList[j]
            val ratio = a.toDouble() / b.toDouble()

            if (!isValidTwoGear(a, b, ratio)) continue

            if (abs(ratio - targetRatio) <= tolerance) {
                val key = "${min(a, b)}-${max(a, b)}"
                if (seen.add(key)) {
                    combinations.add(GearCombination(a, b, null, null, ratio))
                }
            }
        }
    }

    return combinations
}

private fun findFourGearCombinations(
    targetRatio: Double,
    gearList: List<Int>,
    tolerance: Double,
): List<GearCombination> {
    val combinations = mutableListOf<GearCombination>()
    val seen = HashSet<String>()
    val n = gearList.size

    for (i in 0 until n) {
        for (j in 0 until n) {
            for (k in 0 until n) {
                for (l in 0 until n) {
                    if (i == j || i == k || i == l || j == k || j == l || k == l) continue

                    val a = gearList[i]
                    val b = gearList[j]
                    val c = gearList[k]
                    val d = gearList[l]

                    // Ratio: (A * C) / (B * D)
                    val ratio = (a.toDouble() * c.toDouble()) / (b.toDouble() * d.toDouble())

                    if (!isValidFourGear(a, b, c, d, ratio)) continue

                    if (abs(ratio - targetRatio) <= tolerance) {
                        val sorted = listOf(a, b, c, d).sorted()
                        val key = sorted.joinToString("-")
                        if (seen.add(key)) {
                            combinations.add(GearCombination(a, b, c, d, ratio))
                        }
                    }
                }
            }
        }
    }

    return combinations
}

fun findSpurCombinations(z: Int, maxResults: Int = 10): List<GearCombination> {
    val targetRatio = 8.0 / z
    val tolerance = 1e-9

    val twoGear = findTwoGearCombinations(targetRatio, GEAR_LIST_TAKSIMAT, tolerance)
    if (twoGear.size >= maxResults) {
        return twoGear.take(maxResults)
    }

    val fourGear = findFourGearCombinations(targetRatio, GEAR_LIST_TAKSIMAT, tolerance)
    return (twoGear + fourGear).take(maxResults)
}

fun findHelicalCombinations(helicalRatio: Double, maxResults: Int = 10): List<GearCombination> {
    val tolerance = 0.001

    val twoGear = findTwoGearCombinations(helicalRatio, GEAR_LIST_HELICAL, tolerance)
    if (twoGear.size >= maxResults) {
        return twoGear.take(maxResults)
    }

    val fourGear = findFourGearCombinations(helicalRatio, GEAR_LIST_HELICAL, tolerance)
    return (twoGear + fourGear).take(maxResults)
}

fun findTaksimatCombinations(z: Int, maxResults: Int = 10): List<GearCombination> {
    // For helical, taksimat uses 16/Z
    val targetRatio = 16.0 / z
    val tolerance = 1e-9

    val twoGear = findTwoGearCombinations(targetRatio, GEAR_LIST_TAKSIMAT, tolerance)
    if (twoGear.size >= maxResults) {
        return twoGear.take(maxResults)
    }

    val fourGear = findFourGearCombinations(targetRatio, GEAR_LIST_TAKSIMAT, tolerance)
    return (twoGear + fourGear).take(maxResults)
}
