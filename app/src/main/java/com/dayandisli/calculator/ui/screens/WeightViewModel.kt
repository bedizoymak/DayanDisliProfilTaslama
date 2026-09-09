package com.dayandisli.calculator.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayandisli.calculator.domain.CostResult
import com.dayandisli.calculator.domain.GeometryInputs
import com.dayandisli.calculator.domain.GeometryType
import com.dayandisli.calculator.domain.MaterialOption
import com.dayandisli.calculator.domain.VolumeOutcome
import com.dayandisli.calculator.domain.WeightCalculationResult
import com.dayandisli.calculator.domain.calculateVolumeMm3
import com.dayandisli.calculator.domain.computeCost
import com.dayandisli.calculator.domain.computeWeightCalculation
import com.dayandisli.calculator.domain.parseLocaleDouble
import com.dayandisli.calculator.domain.parseLocaleInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class WeightScreenState(
    val result: WeightCalculationResult? = null,
    val validationError: String? = null,
)

/** Ported from WeightCalculation.tsx's local component state + useEffect. */
class WeightViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val geometry: StateFlow<GeometryType> =
        savedStateHandle.getStateFlow(KEY_GEOMETRY, GeometryType.SOLID.name)
            .map { GeometryType.valueOf(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GeometryType.SOLID)

    val diameter: StateFlow<String> = savedStateHandle.getStateFlow(KEY_DIAMETER, "50")
    val length: StateFlow<String> = savedStateHandle.getStateFlow(KEY_LENGTH, "100")
    val outerDiameter: StateFlow<String> = savedStateHandle.getStateFlow(KEY_OUTER_DIAMETER, "60")
    val innerDiameter: StateFlow<String> = savedStateHandle.getStateFlow(KEY_INNER_DIAMETER, "40")
    val width: StateFlow<String> = savedStateHandle.getStateFlow(KEY_WIDTH, "50")
    val height: StateFlow<String> = savedStateHandle.getStateFlow(KEY_HEIGHT, "10")

    val material: StateFlow<MaterialOption> =
        savedStateHandle.getStateFlow(KEY_MATERIAL, MaterialOption.STEEL.name)
            .map { MaterialOption.valueOf(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MaterialOption.STEEL)

    val customDensity: StateFlow<String> = savedStateHandle.getStateFlow(KEY_CUSTOM_DENSITY, "7.85")
    val pricePerKg: StateFlow<String> = savedStateHandle.getStateFlow(KEY_PRICE, "")
    val quantity: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUANTITY, "1")

    fun setGeometry(value: GeometryType) { savedStateHandle[KEY_GEOMETRY] = value.name }
    fun setDiameter(value: String) { savedStateHandle[KEY_DIAMETER] = value }
    fun setLength(value: String) { savedStateHandle[KEY_LENGTH] = value }
    fun setOuterDiameter(value: String) { savedStateHandle[KEY_OUTER_DIAMETER] = value }
    fun setInnerDiameter(value: String) { savedStateHandle[KEY_INNER_DIAMETER] = value }
    fun setWidth(value: String) { savedStateHandle[KEY_WIDTH] = value }
    fun setHeight(value: String) { savedStateHandle[KEY_HEIGHT] = value }
    fun setMaterial(value: MaterialOption) { savedStateHandle[KEY_MATERIAL] = value.name }
    fun setCustomDensity(value: String) { savedStateHandle[KEY_CUSTOM_DENSITY] = value }
    fun setPricePerKg(value: String) { savedStateHandle[KEY_PRICE] = value }
    fun setQuantity(value: String) { savedStateHandle[KEY_QUANTITY] = value }

    val currentDensity: StateFlow<Double> = combine(material, customDensity) { mat, customStr ->
        if (mat == MaterialOption.CUSTOM) parseLocaleDouble(customStr) ?: 0.0 else mat.density
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MaterialOption.STEEL.density)

    val quantityValue: StateFlow<Int> = quantity
        .map { maxOf(1, parseLocaleInt(it) ?: 1) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    private data class RawInputsPart1(val geo: GeometryType, val d: String, val l: String, val od: String, val id: String)
    private data class RawInputs(val part1: RawInputsPart1, val w: String, val h: String)

    private val rawInputsPart1: StateFlow<RawInputsPart1> =
        combine(geometry, diameter, length, outerDiameter, innerDiameter) { geo, d, l, od, id ->
            RawInputsPart1(geo, d, l, od, id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RawInputsPart1(GeometryType.SOLID, "50", "100", "60", "40"))

    private val rawInputs: StateFlow<RawInputs> =
        combine(rawInputsPart1, width, height) { part1, w, h -> RawInputs(part1, w, h) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RawInputs(rawInputsPart1.value, "50", "10"))

    val state: StateFlow<WeightScreenState> = combine(rawInputs, currentDensity, quantityValue) { raw, density, qty ->
        val geo = raw.part1.geo
        val d = raw.part1.d
        val l = raw.part1.l
        val od = raw.part1.od
        val id = raw.part1.id
        val w = raw.w
        val h = raw.h

        if (density <= 0) return@combine WeightScreenState()

        val inputs: GeometryInputs? = when (geo) {
            GeometryType.SOLID -> {
                val dv = parseLocaleDouble(d); val lv = parseLocaleDouble(l)
                if (dv == null || lv == null) null else GeometryInputs.Solid(dv, lv)
            }
            GeometryType.PIPE -> {
                val odv = parseLocaleDouble(od); val idv = parseLocaleDouble(id); val lv = parseLocaleDouble(l)
                if (odv == null || idv == null || lv == null) null else GeometryInputs.Pipe(odv, idv, lv)
            }
            GeometryType.FLAT -> {
                val wv = parseLocaleDouble(w); val hv = parseLocaleDouble(h); val lv = parseLocaleDouble(l)
                if (wv == null || hv == null || lv == null) null else GeometryInputs.Flat(wv, hv, lv)
            }
        }

        if (inputs == null) return@combine WeightScreenState()

        when (val outcome = calculateVolumeMm3(inputs)) {
            is VolumeOutcome.PipeDiameterInvalid -> WeightScreenState(validationError = "Dış çap, iç çaptan büyük olmalıdır")
            is VolumeOutcome.InvalidInputs -> WeightScreenState()
            is VolumeOutcome.Success -> WeightScreenState(result = computeWeightCalculation(outcome.volumeMm3, density, qty))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeightScreenState())

    val cost: StateFlow<CostResult?> = combine(state, pricePerKg, quantityValue) { s, priceStr, qty ->
        val price = parseLocaleDouble(priceStr)
        val result = s.result
        if (result != null && price != null && price > 0) computeCost(result.weightKg, price, qty) else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    companion object {
        private const val KEY_GEOMETRY = "weight_geometry"
        private const val KEY_DIAMETER = "weight_diameter"
        private const val KEY_LENGTH = "weight_length"
        private const val KEY_OUTER_DIAMETER = "weight_outer_diameter"
        private const val KEY_INNER_DIAMETER = "weight_inner_diameter"
        private const val KEY_WIDTH = "weight_width"
        private const val KEY_HEIGHT = "weight_height"
        private const val KEY_MATERIAL = "weight_material"
        private const val KEY_CUSTOM_DENSITY = "weight_custom_density"
        private const val KEY_PRICE = "weight_price"
        private const val KEY_QUANTITY = "weight_quantity"
    }
}
