package com.dayandisli.calculator.domain

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.tan

/** Ported from src/calculator/utils/gearSpur.ts — formulas and rounding must match exactly. */
data class SpurGearResult(
    val mn: Double,
    val z: Int,
    val alphaDeg: Double,
    val alphaRad: Double,
    val diameter: Double,
    val autoCalculatedDiameter: Double,
    val k: Double,
    val kFloor: Int,
    val kCeil: Int,
    val kThird: Int,
    val ew: Double,
    val xFactor: Double,
    val wkFloor: Double,
    val wkCeil: Double,
    val wkThird: Double,
    val taksimatRatio: Double,
)

fun computeSpurParams(
    mn: Double,
    z: Int,
    alphaDeg: Double,
    diameterOverride: Double? = null,
): SpurGearResult {
    val alphaRad = alphaDeg * PI / 180.0

    // Auto-calculated diameter: dA = (Z + 2) * mn
    val autoCalculatedDiameter = (z + 2) * mn
    val diameter = diameterOverride ?: autoCalculatedDiameter

    // k calculation: k = (alpha * Z / 180) + 0.5
    val k = (alphaDeg * z) / 180.0 + 0.5
    val kFloor = floor(k).toInt()
    // Three consecutive shop-floor span measurements, aligned with Reishauer.
    val kCeil = kFloor + 1
    val kThird = kFloor + 2

    // EW (involute function): signed value, matching the shop-floor formula.
    val ew = alphaRad - tan(alphaRad)

    // xFactor (profile/diameter correction)
    val dTarget = autoCalculatedDiameter
    val diff = diameter - dTarget
    val tolerance = 1e-9
    val xFactor = if (abs(diff) > tolerance) diff * kotlin.math.sin(alphaRad) else 0.0

    // Base for Wk calculation
    val base = mn * cos(alphaRad)

    val wkFloor = base * ((kFloor - 0.5) * PI + z * ew) + xFactor
    val wkCeil = base * ((kCeil - 0.5) * PI + z * ew) + xFactor
    val wkThird = base * ((kThird - 0.5) * PI + z * ew) + xFactor

    // Taksimat ratio: 8 / Z
    val taksimatRatio = 8.0 / z

    return SpurGearResult(
        mn = mn,
        z = z,
        alphaDeg = alphaDeg,
        alphaRad = alphaRad,
        diameter = diameter,
        autoCalculatedDiameter = autoCalculatedDiameter,
        k = k,
        kFloor = kFloor,
        kCeil = kCeil,
        kThird = kThird,
        ew = ew,
        xFactor = xFactor,
        wkFloor = wkFloor,
        wkCeil = wkCeil,
        wkThird = wkThird,
        taksimatRatio = taksimatRatio,
    )
}

fun validateSpurInputs(mn: Double, z: Int, diameter: Double): List<String> {
    val errors = mutableListOf<String>()

    if (mn < 0.3 || mn > 3.0) {
        errors.add("Modül 0.3 ile 3.0 arasında olmalıdır")
    }
    if (z < 6) {
        errors.add("Diş sayısı en az 6 olmalıdır")
    }
    if (diameter < 8 || diameter > 250) {
        errors.add("Çap 8 ile 250 mm arasında olmalıdır")
    }

    return errors
}
