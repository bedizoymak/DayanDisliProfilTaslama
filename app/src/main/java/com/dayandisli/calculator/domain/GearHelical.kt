package com.dayandisli.calculator.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/** Ported from src/calculator/utils/gearHelical.ts — formulas, clamping and rounding must match exactly. */
enum class HelixDirection(val label: String) {
    SAG("Sağ"),
    SOL("Sol"),
    ;

    override fun toString(): String = label
}

data class HelicalGearResult(
    val mn: Double,
    val z: Int,
    val alphaDeg: Double,
    val alphaRad: Double,
    val betaDeg: Double,
    val betaRad: Double,
    val helixDirection: HelixDirection,
    val diameter: Double,
    val k: Double,
    val kFloor: Int,
    val kCeil: Int,
    val ew: Double,
    val wkFloor: Double,
    val wkCeil: Double,
    val taksimatRatio: Double,
    val helicalGearRatio: Double,
)

fun computeHelicalParams(
    mn: Double,
    z: Int,
    alphaDeg: Double,
    betaDeg: Double,
    helixDirection: HelixDirection,
): HelicalGearResult {
    val alphaRad = alphaDeg * PI / 180.0
    val betaRad = betaDeg * PI / 180.0

    // Helical diameter: d = (mn / cos(beta)) * Z + 2mn
    val diameter = (mn / cos(betaRad)) * z + 2 * mn

    // x angle calculation: x = arctan(tan(alphaRad) / cos(betaRad))
    val xRad = atan(tan(alphaRad) / cos(betaRad))
    val xDeg = xRad * 180.0 / PI

    // y for EW calculation
    val y = xDeg * PI / 180.0

    // EW helical: |y - tan(xRad)|
    val ew = abs(y - tan(xRad))

    // k calculation: k = (alpha * Z / 180) + 0.5
    val k = (alphaDeg * z) / 180.0 + 0.5
    var kFloor = floor(k).toInt()
    var kCeil = ceil(k).toInt()

    // Clamp k values: kFloor >= 2, kCeil >= 3
    if (kFloor < 2) kFloor = 2
    if (kCeil < 3) kCeil = 3

    val base = mn * cos(alphaRad)

    val wkFloor = base * ((kFloor - 0.5) * PI + z * ew)
    val wkCeil = base * ((kCeil - 0.5) * PI + z * ew)

    // Taksimat ratio: 16 / Z (for helical)
    val taksimatRatio = 16.0 / z

    // Helical gear ratio: sin(betaRad) * 18 / (pi * mn), rounded to 6 decimals like the source's toFixed(6)
    val rawRatio = (sin(betaRad) * 18.0) / (PI * mn)
    val helicalGearRatio = roundToFixed(rawRatio, 6)

    return HelicalGearResult(
        mn = mn,
        z = z,
        alphaDeg = alphaDeg,
        alphaRad = alphaRad,
        betaDeg = betaDeg,
        betaRad = betaRad,
        helixDirection = helixDirection,
        diameter = diameter,
        k = k,
        kFloor = kFloor,
        kCeil = kCeil,
        ew = ew,
        wkFloor = wkFloor,
        wkCeil = wkCeil,
        taksimatRatio = taksimatRatio,
        helicalGearRatio = helicalGearRatio,
    )
}

/** Inverts helicalGearRatio = sin(betaRad) * 18 / (pi * mn) to recover the helix angle a given
 * gear-combination ratio would actually produce: betaActual = asin(ratio * pi * mn / 18).
 * Returns null if the asin input is out of [-1, 1]. */
fun helicalActualHelixAngleDeg(mn: Double, combinationRatio: Double): Double? {
    val sinBeta = combinationRatio * PI * mn / 18.0
    if (sinBeta < -1.0 || sinBeta > 1.0) return null
    return asin(sinBeta) * 180.0 / PI
}

fun validateHelicalInputs(mn: Double, z: Int, betaDeg: Double): List<String> {
    val errors = mutableListOf<String>()

    if (mn < 0.3 || mn > 3.0) {
        errors.add("Modül 0.3 ile 3.0 arasında olmalıdır")
    }
    if (z < 6) {
        errors.add("Diş sayısı en az 6 olmalıdır")
    }
    if (betaDeg < 0 || betaDeg > 45) {
        errors.add("Helis açısı 0 ile 45 derece arasında olmalıdır")
    }

    return errors
}
