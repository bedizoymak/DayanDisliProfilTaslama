package com.dayandisli.calculator.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Reishauer profile-grinding calculator. Ported from CEHA-AndroidApp's ReishauerCalculator.kt /
 * ReishauerAngleParser / ReishauerGearCombinations, adapted to this app's Double-based domain
 * conventions (see GearHelical.kt) instead of BigDecimal, and to the shared [GearCombination]
 * presentation type used across Spur/Helical. This is a distinct machine standard from Küçük
 * Azdırma / Helical Hobbing (GearHelical.kt) and does not alter that file's formulas.
 */

private fun canonicalizeReishauerAngleSeparators(input: String): String = input
    .replace(Regex("(?i)\\s*cs\\b"), "")
    .replace("'''", ",")
    .replace("''", ",")
    .replace("′", ",")
    .replace("″", ",")
    .replace("'", ",")
    .replace("\"", ",")
    .replace("°", ",")
    .replace(Regex("(?<=\\d)[\\s.]+(?=\\d)"), ",")

/**
 * Formats a permissively entered D,M,S,CS angle into the fixed shop-floor notation used by the
 * Reishauer form. For example, `20.35,6,95` and `20 35 6 95` become `20°35′06″95`.
 */
fun normalizeReishauerAngleInput(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return ""
    val parts = canonicalizeReishauerAngleSeparators(trimmed)
        .split(Regex("[,.\\s]+"))
        .filter { it.isNotEmpty() }
    if (parts.isEmpty() || parts.size > 4 || parts.any { it.toIntOrNull() == null }) return trimmed

    val values = parts.map { it.toInt() }.toMutableList()
    while (values.size < 4) values.add(0)
    return "${values[0]}°${values[1].toString().padStart(2, '0')}′${values[2].toString().padStart(2, '0')}″${values[3].toString().padStart(2, '0')}"
}

/**
 * Keeps angle entry fluid while typing: when the user types a dot, comma, or whitespace after a
 * component, it is immediately replaced with the next engineering angle symbol. The remaining
 * component stays editable, while [normalizeReishauerAngleInput] fills its trailing zeros once
 * entry is complete.
 */
fun normalizeReishauerAngleSeparator(input: String): String {
    if (input.isEmpty() || !(input.last().isWhitespace() || input.last() == '.' || input.last() == ',')) return input
    val withoutTrailingSeparators = input.trimEnd { it.isWhitespace() || it == '.' || it == ',' }
    val parts = canonicalizeReishauerAngleSeparators(withoutTrailingSeparators)
        .split(Regex("[,.\\s]+"))
        .filter { it.isNotEmpty() }
    if (parts.isEmpty() || parts.size > 4 || parts.any { it.toIntOrNull() == null }) return input

    val values = parts.map { it.toInt() }
    return buildString {
        append(values[0])
        append('°')
        if (values.size >= 2) {
            append(values[1].toString().padStart(2, '0'))
            append('′')
        }
        if (values.size >= 3) {
            append(values[2].toString().padStart(2, '0'))
            append('″')
        }
        if (values.size >= 4) {
            append(values[3].toString().padStart(2, '0'))
        }
    }
}

/** D,M,S,CS (Derece,Dakika,Saniye,Salise) angle parser. Missing/empty trailing components are
 * zero; more than 4 comma-separated components or any non-numeric component is rejected. Each
 * physical component is also range-checked (minute/second 0..59, centisecond 0..99) so malformed
 * input like "20,60" or "20,30,45,100" is rejected rather than silently accepted as a valid but
 * out-of-range angle. */
fun parseReishauerAngle(input: String): Double? {
    val canonical = canonicalizeReishauerAngleSeparators(input.trim()).replace(Regex("[.\\s]+"), ",").trim(',')
    val parts = canonical.split(",").map { it.trim() }
    if (parts.size > 4) return null

    val components = mutableListOf<Double>()
    for (part in parts) {
        if (part.isEmpty()) {
            components.add(0.0)
        } else {
            components.add(part.toDoubleOrNull() ?: return null)
        }
    }
    while (components.size < 4) components.add(0.0)

    val (degrees, minutes, seconds, centiseconds) = components
    if (minutes < 0.0 || minutes > 59.0) return null
    if (seconds < 0.0 || seconds > 59.0) return null
    if (centiseconds < 0.0 || centiseconds > 99.0) return null

    return degrees + minutes / 60.0 + seconds / 3600.0 + centiseconds / 360000.0
}

/** Rounds [value] down (toward negative infinity is irrelevant here since values are positive;
 * this truncates like BigDecimal's RoundingMode.DOWN) to [digits] decimals. */
fun floorToFixed(value: Double, digits: Int): Double {
    if (value.isNaN() || value.isInfinite()) return value
    return BigDecimal(value).setScale(digits, RoundingMode.DOWN).toDouble()
}

data class ReishauerResult(
    val mn: Double,
    val z: Int,
    val alphaDeg: Double,
    val alphaRaw: String,
    val betaDeg: Double,
    val betaRaw: String,
    val helixDirection: HelixDirection,
    val nominalDiameter: Double,
    val diameter: Double,
    val k1: Int,
    val wk1: Double,
    val k2: Int,
    val wk2: Double,
    val k3: Int,
    val wk3: Double,
    val taksimatRatio: Double,
    val helixGearRatio: Double,
    /** Full-precision helis ratio (mn / (11.6909 * sin(beta))), before the display-only floor-to-9-
     * decimals rounding applied to [helixGearRatio]. Combination search must rank candidates against
     * this value, not the rounded display value. */
    val helixGearRatioFullPrecision: Double,
)

fun validateReishauerInputs(
    mn: Double?,
    z: Int?,
    manualDiameter: Double?,
    alphaDeg: Double?,
    betaDeg: Double?,
): List<String> {
    val errors = mutableListOf<String>()
    if (mn == null || mn < 0.3 || mn > 3.0) errors.add("Modül 0.3 ile 3.0 arasında olmalıdır")
    if (z == null || z < 6) errors.add("Diş sayısı en az 6 olmalıdır")
    if (manualDiameter == null || manualDiameter < 8.0 || manualDiameter > 250.0) {
        errors.add("Çap (ø) 8 ile 250 mm arasında olmalıdır")
    }
    if (alphaDeg == null) errors.add("Kavrama açısı (α) formatı geçersiz (Derece,Dakika,Saniye,Salise)")
    if (betaDeg == null) errors.add("Helis açısı (β) formatı geçersiz (Derece,Dakika,Saniye,Salise)")
    return errors
}

fun computeReishauerNominalDiameter(mn: Double, z: Int, betaDeg: Double): Double {
    val betaRad = betaDeg * PI / 180.0
    val cosBeta = cos(betaRad)
    if (cosBeta == 0.0) return 0.0
    return (mn / cosBeta) * z + 2 * mn
}

fun computeReishauerParams(
    mn: Double,
    z: Int,
    alphaDeg: Double,
    alphaRaw: String,
    betaDeg: Double,
    betaRaw: String,
    helixDirection: HelixDirection,
    manualDiameter: Double,
): ReishauerResult {
    val alphaRad = alphaDeg * PI / 180.0
    val betaRad = betaDeg * PI / 180.0

    val nominalDiameter = roundToFixed(computeReishauerNominalDiameter(mn, z, betaDeg), 4)
    val manualDiameterRounded = roundToFixed(manualDiameter, 4)

    // Transformed pressure angle / helicalEw (identical formula to Helical Hobbing).
    val xRad = atan(tan(alphaRad) / cos(betaRad))
    val xDeg = xRad * 180.0 / PI
    val y = xDeg * PI / 180.0
    val ew = abs(y - tan(xRad))

    // Three consecutive k values.
    val kRaw = (alphaDeg * z) / 180.0 + 0.5
    val k1 = kotlin.math.floor(kRaw).toInt()
    val k2 = k1 + 1
    val k3 = k1 + 2

    val cosAlpha = cos(alphaRad)
    fun wk(k: Int): Double = mn * cosAlpha * (((k - 0.5) * PI) + z * ew)

    val correction = (manualDiameterRounded - nominalDiameter) * sin(alphaRad)

    val wk1 = roundToFixed(wk(k1) + correction, 4)
    val wk2 = roundToFixed(wk(k2) + correction, 4)
    val wk3 = roundToFixed(wk(k3) + correction, 4)

    val sinBeta = sin(betaRad)
    val helixGearRatioFullPrecision = if (sinBeta == 0.0) 0.0 else mn / (11.6909 * sinBeta)
    val helixGearRatio = if (sinBeta == 0.0) 0.0 else floorToFixed(helixGearRatioFullPrecision, 9)

    // Taksimat Oranı: 24 / Z (Reishauer-specific target-ratio convention; distinct from Küçük
    // Azdırma / Helical Hobbing's 16 / Z used in GearHelical.kt, which must not change).
    val taksimatRatio = if (z != 0) 24.0 / z else 0.0

    return ReishauerResult(
        mn = mn,
        z = z,
        alphaDeg = alphaDeg,
        alphaRaw = alphaRaw,
        betaDeg = betaDeg,
        betaRaw = betaRaw,
        helixDirection = helixDirection,
        nominalDiameter = nominalDiameter,
        diameter = manualDiameterRounded,
        k1 = k1,
        wk1 = wk1,
        k2 = k2,
        wk2 = wk2,
        k3 = k3,
        wk3 = wk3,
        taksimatRatio = taksimatRatio,
        helixGearRatio = helixGearRatio,
        helixGearRatioFullPrecision = helixGearRatioFullPrecision,
    )
}

/** Reishauer's single shared physical gear inventory, used for both Taksimat and Helis
 * combination searches (25..127 inclusive, matching CEHA's REISHAUER_GEAR_LIST). */
val REISHAUER_GEAR_LIST: List<Int> = (25..127).toList()

private fun reishauerTwoGear(
    gearList: List<Int>,
    targetRatio: Double,
    tolerance: Double,
    maxResults: Int,
): List<GearCombination> {
    val results = mutableListOf<GearCombination>()
    val seen = HashSet<String>()
    outer@ for (i in gearList.indices) {
        for (j in i + 1 until gearList.size) {
            val a = gearList[i]
            val b = gearList[j]
            val ratio: Double
            val ga: Int
            val gb: Int
            if (targetRatio >= 1) {
                ratio = b.toDouble() / a.toDouble()
                ga = b; gb = a
            } else {
                ratio = a.toDouble() / b.toDouble()
                ga = a; gb = b
            }
            if (abs(ratio - targetRatio) <= tolerance) {
                val key = "$ga/$gb"
                if (seen.add(key)) results.add(GearCombination(ga, gb, null, null, ratio))
            }
            if (results.size >= maxResults) break@outer
        }
    }
    return results
}

private fun reishauerFourGear(
    gearList: List<Int>,
    targetRatio: Double,
    tolerance: Double,
    maxResults: Int,
): List<GearCombination> {
    val results = mutableListOf<GearCombination>()
    val seen = HashSet<String>()
    val n = gearList.size
    outer@ for (i in 0 until n - 3) {
        for (j in i + 1 until n - 2) {
            for (k in j + 1 until n - 1) {
                for (l in k + 1 until n) {
                    val a = gearList[i]; val b = gearList[j]; val c = gearList[k]; val d = gearList[l]
                    val ratio: Double
                    val ga: Int; val gb: Int; val gc: Int; val gd: Int
                    if (targetRatio >= 1) {
                        ratio = (b.toDouble() / a.toDouble()) * (d.toDouble() / c.toDouble())
                        ga = b; gb = a; gc = d; gd = c
                    } else {
                        ratio = (a.toDouble() / b.toDouble()) * (c.toDouble() / d.toDouble())
                        ga = a; gb = b; gc = c; gd = d
                    }
                    if (abs(ratio - targetRatio) <= tolerance) {
                        val key = "$ga/$gb*$gc/$gd"
                        if (seen.add(key)) results.add(GearCombination(ga, gb, gc, gd, ratio))
                    }
                    if (results.size >= maxResults) break@outer
                }
            }
        }
    }
    return results
}

/** 2-gear combinations first; 4-gear combinations only if fewer than 5 valid 2-gear results
 * exist; max 5 results overall. Shared by both Taksimat and Helis searches. */
private fun findReishauerCombinations(targetRatio: Double, tolerance: Double, maxResults: Int = 5): List<GearCombination> {
    val twoGear = reishauerTwoGear(REISHAUER_GEAR_LIST, targetRatio, tolerance, maxResults)
    if (twoGear.size >= 5) return twoGear.take(maxResults)
    val fourGear = reishauerFourGear(REISHAUER_GEAR_LIST, targetRatio, tolerance, maxResults)
    return (twoGear + fourGear).take(maxResults)
}

fun findReishauerTaksimatCombinations(z: Int): List<GearCombination> {
    val targetRatio = if (z != 0) 24.0 / z else 0.0
    return findReishauerCombinations(targetRatio, tolerance = 1e-9)
}

/** Number of best-by-error candidates displayed on the Helis Çark Kombinasyonları card list. */
private const val HELIS_DISPLAY_COUNT = 20

/** Internal ranked-candidate buffer size, larger than [HELIS_DISPLAY_COUNT] so candidates
 * excluded by the actual-angle asin-validity filter can still be backfilled from the ranked pool
 * instead of shrinking the final displayed list below its intended size. */
private const val HELIS_INTERNAL_CANDIDATE_COUNT = 40

/**
 * Ranks every feasible 2-gear and 4-gear combination from [gearList] against the full-precision
 * [targetRatio] by ascending absolute error and returns the [maxResults] best. This is an
 * exhaustive search: no tolerance cutoff, and it never stops early after finding some number of
 * 2-gear matches, so a genuinely closer 4-gear combination can never be shadowed by an earlier,
 * less accurate 2-gear one. A bounded max-heap (capacity [maxResults]) is used so the O(n^4)
 * 4-gear scan never has to materialize or sort the full candidate space - only the running best
 * few are ever retained. Invalid candidates (repeated tooth-count values, a trivial 1:1 stage
 * ratio, or a trivial 1:1 overall train ratio) are rejected before ever reaching the heap, exactly
 * as [findTwoGearCombinations]/[findFourGearCombinations] already do for Spur/Helical.
 */
internal fun rankReishauerCombinations(
    gearList: List<Int>,
    targetRatio: Double,
    maxResults: Int,
): List<GearCombination> {
    data class RankedCombo(val a: Int, val b: Int, val c: Int?, val d: Int?, val ratio: Double, val error: Double)

    val heap = java.util.PriorityQueue<RankedCombo>(maxResults + 1, compareByDescending { it.error })

    fun considerAndMaybeInsert(a: Int, b: Int, c: Int?, d: Int?, ratio: Double) {
        val error = abs(ratio - targetRatio)
        if (heap.size < maxResults) {
            heap.add(RankedCombo(a, b, c, d, ratio, error))
        } else if (error < heap.peek()!!.error) {
            heap.poll()
            heap.add(RankedCombo(a, b, c, d, ratio, error))
        }
    }

    // Every REISHAUER_GEAR_LIST value is distinct and each strictly-increasing index tuple
    // (i<j) / (i<j<k<l) maps to one unique physical gear arrangement, so no separate "seen"
    // de-duplication is needed here (unlike the Spur/Helical inventories, which contain
    // duplicate tooth-count values at different list positions).
    for (i in gearList.indices) {
        for (j in i + 1 until gearList.size) {
            val a = gearList[i]
            val b = gearList[j]
            val ratio: Double
            val ga: Int
            val gb: Int
            if (targetRatio >= 1) {
                ratio = b.toDouble() / a.toDouble()
                ga = b; gb = a
            } else {
                ratio = a.toDouble() / b.toDouble()
                ga = a; gb = b
            }
            if (!isValidTwoGear(ga, gb, ratio)) continue
            considerAndMaybeInsert(ga, gb, null, null, ratio)
        }
    }

    val n = gearList.size
    for (i in 0 until n - 3) {
        for (j in i + 1 until n - 2) {
            for (k in j + 1 until n - 1) {
                for (l in k + 1 until n) {
                    val a = gearList[i]; val b = gearList[j]; val c = gearList[k]; val d = gearList[l]
                    val ratio: Double
                    val ga: Int; val gb: Int; val gc: Int; val gd: Int
                    if (targetRatio >= 1) {
                        ratio = (b.toDouble() / a.toDouble()) * (d.toDouble() / c.toDouble())
                        ga = b; gb = a; gc = d; gd = c
                    } else {
                        ratio = (a.toDouble() / b.toDouble()) * (c.toDouble() / d.toDouble())
                        ga = a; gb = b; gc = c; gd = d
                    }
                    if (!isValidFourGear(ga, gb, gc, gd, ratio)) continue
                    considerAndMaybeInsert(ga, gb, gc, gd, ratio)
                }
            }
        }
    }

    return heap.sortedBy { it.error }.map { GearCombination(it.a, it.b, it.c, it.d, it.ratio) }
}

/** The actual helix angle (degrees) a given combination ratio would produce:
 * betaActual = asin(mn / (11.6909 * ratio)). Returns null if the denominator or ratio is zero, or
 * if the asin input is out of [-1, 1]. */
fun reishauerActualHelixAngle(mn: Double, combinationRatio: Double): Double? {
    if (combinationRatio == 0.0) return null
    val denominator = 11.6909 * combinationRatio
    if (denominator == 0.0) return null
    val asinInput = mn / denominator
    if (asinInput < -1.0 || asinInput > 1.0) return null
    return asin(asinInput) * 180.0 / PI
}

/**
 * A single Helis Çark Kombinasyonları candidate, enriched with everything the receipt card needs
 * to render: [combination] (whose `ratio` is the display value, floored to 9 decimals), the
 * [absoluteError] against the full-precision target ratio, the actual helix angle this
 * combination produces, its deviation from the requested β, and whether it is a genuinely exact
 * (zero-error) mathematical match. Mirrors CEHA's `ReishauerHelisCombination`.
 */
data class ReishauerHelisCombination(
    val combination: GearCombination,
    val absoluteError: Double,
    val actualHelixAngleDeg: Double,
    val deviationDeg: Double,
    val isPerfectMatch: Boolean,
)

/**
 * Helis Çark Kombinasyonları: exhaustive search against the full-precision [targetRatioFullPrecision]
 * (never the display-rounded [ReishauerResult.helixGearRatio]), 2-gear and 4-gear candidates ranked
 * together by ascending absolute error - never cut off after a fixed number of preliminary/2-gear
 * matches, so a closer 4-gear result always outranks a weaker 2-gear one. A buffer beyond
 * [maxResults] is requested internally so a candidate excluded because its actual helix angle falls
 * outside the valid asin domain can still be backfilled from the ranked pool instead of shrinking
 * the final list. Each surviving candidate is enriched into a [ReishauerHelisCombination] (actual
 * angle, deviation from [betaRequestedDeg], and exact-match flag) rather than left as a raw string.
 */
fun findReishauerHelisCombinations(
    mn: Double,
    betaRequestedDeg: Double,
    targetRatioFullPrecision: Double,
    maxResults: Int = HELIS_DISPLAY_COUNT,
    internalBufferSize: Int = HELIS_INTERNAL_CANDIDATE_COUNT,
): List<ReishauerHelisCombination> {
    val ranked = rankReishauerCombinations(REISHAUER_GEAR_LIST, targetRatioFullPrecision, internalBufferSize)
    return ranked.mapNotNull { combo ->
        val actualAngle = reishauerActualHelixAngle(mn, combo.ratio) ?: return@mapNotNull null
        val error = abs(combo.ratio - targetRatioFullPrecision)
        ReishauerHelisCombination(
            combination = combo.copy(ratio = floorToFixed(combo.ratio, 9)),
            absoluteError = error,
            actualHelixAngleDeg = actualAngle,
            deviationDeg = actualAngle - betaRequestedDeg,
            isPerfectMatch = error == 0.0,
        )
    }.take(maxResults)
}
