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

/** D,M,S,CS (Derece,Dakika,Saniye,Salise) angle parser. Missing/empty trailing components are
 * zero; more than 4 comma-separated components or any non-numeric component is rejected. */
fun parseReishauerAngle(input: String): Double? {
    val parts = input.split(",").map { it.trim() }
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
    val helixGearRatio = if (sinBeta == 0.0) 0.0 else floorToFixed(mn / (11.6909 * sinBeta), 9)

    val taksimatRatio = if (z != 0) 16.0 / z else 0.0

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
    val targetRatio = if (z != 0) 16.0 / z else 0.0
    return findReishauerCombinations(targetRatio, tolerance = 1e-9)
}

fun findReishauerHelisCombinations(helixGearRatio: Double): List<GearCombination> =
    findReishauerCombinations(helixGearRatio, tolerance = 0.001)

/** The actual helix angle (degrees) a given combination ratio would produce:
 * betaActual = asin(mn / (11.6909 * ratio)). Returns null if the asin input is out of [-1, 1]. */
fun reishauerActualHelixAngle(mn: Double, combinationRatio: Double): Double? {
    val denominator = 11.6909 * combinationRatio
    if (denominator == 0.0) return null
    val asinInput = mn / denominator
    if (asinInput < -1.0 || asinInput > 1.0) return null
    return asin(asinInput) * 180.0 / PI
}
