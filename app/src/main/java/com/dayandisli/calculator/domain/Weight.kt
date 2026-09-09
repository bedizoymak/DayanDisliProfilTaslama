package com.dayandisli.calculator.domain

import kotlin.math.PI

/** Ported from src/calculator/utils/weight.ts (solid cylinder) plus the pipe/flat-bar geometry
 * and quantity/cost logic that WeightCalculation.tsx implements inline in calculateVolume(). */

data class WeightResult(
    val volumeMm3: Double,
    val volumeCm3: Double,
    val weightGrams: Double,
    val weightKg: Double,
)

/** Solid-cylinder-only computation, matching computeWeight() in weight.ts exactly. */
fun computeWeight(diameter: Double, length: Double, density: Double): WeightResult {
    val radius = diameter / 2.0
    val volumeMm3 = PI * radius * radius * length
    val volumeCm3 = volumeMm3 / 1000.0
    val weightGrams = volumeCm3 * density
    val weightKg = weightGrams / 1000.0
    return WeightResult(volumeMm3, volumeCm3, weightGrams, weightKg)
}

/** Common material densities (g/cm^3), matches MATERIAL_DENSITIES in weight.ts exactly. */
object MaterialDensities {
    const val STEEL = 7.85
    const val STAINLESS_STEEL = 8.0
    const val ALUMINUM = 2.7
    const val BRASS = 8.5
    const val BRONZE = 8.8
    const val COPPER = 8.96
    const val CAST_IRON = 7.2
    const val TITANIUM = 4.5
}

enum class MaterialOption(val label: String, val density: Double) {
    STEEL("Çelik", MaterialDensities.STEEL),
    STAINLESS_STEEL("Paslanmaz Çelik", MaterialDensities.STAINLESS_STEEL),
    ALUMINUM("Alüminyum", MaterialDensities.ALUMINUM),
    BRASS("Pirinç", MaterialDensities.BRASS),
    BRONZE("Bronz", MaterialDensities.BRONZE),
    COPPER("Bakır", MaterialDensities.COPPER),
    CAST_IRON("Dökme Demir", MaterialDensities.CAST_IRON),
    TITANIUM("Titanyum", MaterialDensities.TITANIUM),
    CUSTOM("Özel", 7.85),
}

enum class GeometryType(val label: String) {
    SOLID("Silindir"),
    PIPE("Boru"),
    FLAT("Lama"),
}

sealed class GeometryInputs {
    data class Solid(val diameter: Double, val length: Double) : GeometryInputs()
    data class Pipe(val outerDiameter: Double, val innerDiameter: Double, val length: Double) : GeometryInputs()
    data class Flat(val width: Double, val height: Double, val length: Double) : GeometryInputs()
}

sealed class VolumeOutcome {
    data class Success(val volumeMm3: Double) : VolumeOutcome()
    /** Mirrors WeightCalculation.tsx's inline validation: outer diameter must exceed inner diameter for a pipe. */
    object PipeDiameterInvalid : VolumeOutcome()
    object InvalidInputs : VolumeOutcome()
}

/** Mirrors WeightCalculation.tsx's calculateVolume(). All three geometries must produce a
 * strictly-positive volume from strictly-positive dimensions or the calculation is invalid. */
fun calculateVolumeMm3(inputs: GeometryInputs): VolumeOutcome {
    return when (inputs) {
        is GeometryInputs.Solid -> {
            if (inputs.diameter <= 0 || inputs.length <= 0) return VolumeOutcome.InvalidInputs
            VolumeOutcome.Success(PI * Math.pow(inputs.diameter / 2.0, 2.0) * inputs.length)
        }
        is GeometryInputs.Pipe -> {
            if (inputs.outerDiameter <= 0 || inputs.innerDiameter <= 0 || inputs.length <= 0) {
                return VolumeOutcome.InvalidInputs
            }
            if (inputs.outerDiameter <= inputs.innerDiameter) {
                return VolumeOutcome.PipeDiameterInvalid
            }
            val volume = (PI / 4.0) * (Math.pow(inputs.outerDiameter, 2.0) - Math.pow(inputs.innerDiameter, 2.0)) * inputs.length
            VolumeOutcome.Success(volume)
        }
        is GeometryInputs.Flat -> {
            if (inputs.width <= 0 || inputs.height <= 0 || inputs.length <= 0) return VolumeOutcome.InvalidInputs
            VolumeOutcome.Success(inputs.width * inputs.height * inputs.length)
        }
    }
}

data class WeightCalculationResult(
    val volumeMm3: Double,
    val volumeCm3: Double,
    val weightGrams: Double,
    val weightKg: Double,
    val totalWeightGrams: Double,
    val totalWeightKg: Double,
)

/** Mirrors the useEffect in WeightCalculation.tsx that derives weight from volume, density and quantity. */
fun computeWeightCalculation(volumeMm3: Double, density: Double, quantity: Int): WeightCalculationResult {
    val effectiveQuantity = max(1, quantity)
    val volumeCm3 = volumeMm3 / 1000.0
    val weightGrams = volumeCm3 * density
    val weightKg = weightGrams / 1000.0
    val totalWeightGrams = weightGrams * effectiveQuantity
    val totalWeightKg = weightKg * effectiveQuantity
    return WeightCalculationResult(volumeMm3, volumeCm3, weightGrams, weightKg, totalWeightGrams, totalWeightKg)
}

private fun max(a: Int, b: Int): Int = if (a > b) a else b

data class CostResult(val singleCost: Double, val totalCost: Double)

/** Mirrors WeightCalculation.tsx's showCost/singleCost/totalCost derivation. Only meaningful
 * when pricePerKg is a finite, strictly-positive number — callers should gate display on that. */
fun computeCost(weightKg: Double, pricePerKg: Double, quantity: Int): CostResult {
    val effectiveQuantity = max(1, quantity)
    val singleCost = weightKg * pricePerKg
    val totalCost = singleCost * effectiveQuantity
    return CostResult(singleCost, totalCost)
}
