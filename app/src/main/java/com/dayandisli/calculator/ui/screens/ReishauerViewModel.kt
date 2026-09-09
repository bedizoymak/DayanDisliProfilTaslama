package com.dayandisli.calculator.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayandisli.calculator.domain.GearCombination
import com.dayandisli.calculator.domain.HelixDirection
import com.dayandisli.calculator.domain.ReishauerHelisCombination
import com.dayandisli.calculator.domain.ReishauerResult
import com.dayandisli.calculator.domain.computeReishauerNominalDiameter
import com.dayandisli.calculator.domain.computeReishauerParams
import com.dayandisli.calculator.domain.findReishauerHelisCombinations
import com.dayandisli.calculator.domain.findReishauerTaksimatCombinations
import com.dayandisli.calculator.domain.normalizeDecimalInput
import com.dayandisli.calculator.domain.parseLocaleDouble
import com.dayandisli.calculator.domain.parseLocaleInt
import com.dayandisli.calculator.domain.parseReishauerAngle
import com.dayandisli.calculator.domain.validateReishauerInputs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Reishauer taşlama form/receipt state, following the same StateFlow-based conventions as
 * SpurGearViewModel / HelicalGearViewModel. Diameter is auto-calculated from mn/Z/beta but can be
 * overridden manually (the override field takes precedence when non-blank). */
class ReishauerViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val mn: StateFlow<String> = savedStateHandle.getStateFlow(KEY_MN, "2.5")
    val z: StateFlow<String> = savedStateHandle.getStateFlow(KEY_Z, "20")
    val alphaRaw: StateFlow<String> = savedStateHandle.getStateFlow(KEY_ALPHA, "20°00′00″00")
    val betaRaw: StateFlow<String> = savedStateHandle.getStateFlow(KEY_BETA, "15°00′00″00")
    val diameterOverride: StateFlow<String> = savedStateHandle.getStateFlow(KEY_DIAMETER, "")
    val helixDirection: StateFlow<HelixDirection> =
        savedStateHandle.getStateFlow(KEY_DIRECTION, HelixDirection.SAG.name)
            .map { HelixDirection.valueOf(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HelixDirection.SAG)

    fun setMn(value: String) { savedStateHandle[KEY_MN] = normalizeDecimalInput(value) }
    fun setZ(value: String) { savedStateHandle[KEY_Z] = value }
    fun setAlphaRaw(value: String) { savedStateHandle[KEY_ALPHA] = value }
    fun setBetaRaw(value: String) { savedStateHandle[KEY_BETA] = value }
    fun setDiameterOverride(value: String) { savedStateHandle[KEY_DIAMETER] = value }
    fun setHelixDirection(value: HelixDirection) { savedStateHandle[KEY_DIRECTION] = value.name }

    /** Auto-calculated nominal diameter shown in the form as a live preview; null while mn/z/beta
     * are incomplete or invalid. */
    val autoDiameter: StateFlow<Double?> = combine(mn, z, betaRaw) { mnStr, zStr, betaStr ->
        val mnVal = parseLocaleDouble(mnStr)
        val zVal = parseLocaleInt(zStr)
        val betaVal = parseReishauerAngle(betaStr)
        if (mnVal == null || zVal == null || betaVal == null) null else computeReishauerNominalDiameter(mnVal, zVal, betaVal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val effectiveDiameter: StateFlow<Double?> = combine(diameterOverride, autoDiameter) { override, auto ->
        if (override.isBlank()) auto else parseLocaleDouble(override)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val errors: StateFlow<List<String>> = combine(mn, z, effectiveDiameter, alphaRaw, betaRaw) { mnStr, zStr, diameter, alphaStr, betaStr ->
        validateReishauerInputs(
            mn = parseLocaleDouble(mnStr),
            z = parseLocaleInt(zStr),
            manualDiameter = diameter,
            alphaDeg = parseReishauerAngle(alphaStr),
            betaDeg = parseReishauerAngle(betaStr),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class FormSnapshot(
        val mnStr: String,
        val zStr: String,
        val alphaStr: String,
        val betaStr: String,
        val direction: HelixDirection,
    )

    private val formSnapshot: StateFlow<FormSnapshot> =
        combine(mn, z, alphaRaw, betaRaw, helixDirection) { mnStr, zStr, alphaStr, betaStr, direction ->
            FormSnapshot(mnStr, zStr, alphaStr, betaStr, direction)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FormSnapshot("", "", "", "", HelixDirection.SAG))

    /** Only non-null once validation passes, so navigation to the receipt is gated on validity
     * (mirrors SpurGearViewModel/HelicalGearViewModel: the "view receipt" CTA is only shown when
     * result != null, and the CEHA bug of navigating before validating is fixed by construction). */
    val result: StateFlow<ReishauerResult?> =
        combine(formSnapshot, effectiveDiameter) { snapshot, diameter ->
            val mnVal = parseLocaleDouble(snapshot.mnStr)
            val zVal = parseLocaleInt(snapshot.zStr)
            val alphaVal = parseReishauerAngle(snapshot.alphaStr)
            val betaVal = parseReishauerAngle(snapshot.betaStr)

            val hasErrors = validateReishauerInputs(mnVal, zVal, diameter, alphaVal, betaVal).isNotEmpty()
            if (mnVal == null || zVal == null || alphaVal == null || betaVal == null || diameter == null || hasErrors) {
                null
            } else {
                computeReishauerParams(mnVal, zVal, alphaVal, snapshot.alphaStr, betaVal, snapshot.betaStr, snapshot.direction, diameter)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _taksimatCombinations = MutableStateFlow<List<GearCombination>?>(null)
    val taksimatCombinations: StateFlow<List<GearCombination>?> = _taksimatCombinations.asStateFlow()

    private val _helisCombinations = MutableStateFlow<List<ReishauerHelisCombination>?>(null)
    val helisCombinations: StateFlow<List<ReishauerHelisCombination>?> = _helisCombinations.asStateFlow()

    private var combinationsJob: Job? = null
    private var combinationsForKey: List<Any>? = null

    /** [helixGearRatioFullPrecision] (not the display-rounded ratio) is used as the Helis search
     * target, per [com.dayandisli.calculator.domain.findReishauerHelisCombinations]. */
    fun ensureCombinationsFor(z: Int, mn: Double, betaRequestedDeg: Double, helixGearRatioFullPrecision: Double) {
        val key = listOf(z, mn, betaRequestedDeg, helixGearRatioFullPrecision)
        if (combinationsForKey == key && _taksimatCombinations.value != null && _helisCombinations.value != null) return
        combinationsJob?.cancel()
        combinationsForKey = key
        _taksimatCombinations.value = null
        _helisCombinations.value = null
        combinationsJob = viewModelScope.launch {
            val taksimat = withContext(Dispatchers.Default) { findReishauerTaksimatCombinations(z) }
            _taksimatCombinations.value = taksimat
            val helis = withContext(Dispatchers.Default) {
                findReishauerHelisCombinations(mn, betaRequestedDeg, helixGearRatioFullPrecision)
            }
            _helisCombinations.value = helis
        }
    }

    companion object {
        private const val KEY_MN = "reishauer_mn"
        private const val KEY_Z = "reishauer_z"
        private const val KEY_ALPHA = "reishauer_alpha"
        private const val KEY_BETA = "reishauer_beta"
        private const val KEY_DIAMETER = "reishauer_diameter"
        private const val KEY_DIRECTION = "reishauer_direction"
    }
}
