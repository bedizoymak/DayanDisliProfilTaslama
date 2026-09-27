package com.dayandisli.calculator.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayandisli.calculator.domain.GearCombination
import com.dayandisli.calculator.domain.SpurGearResult
import com.dayandisli.calculator.domain.computeSpurParams
import com.dayandisli.calculator.domain.findSpurCombinations
import com.dayandisli.calculator.domain.parseLocaleDouble
import com.dayandisli.calculator.domain.parseLocaleInt
import com.dayandisli.calculator.domain.parseReishauerAngle
import com.dayandisli.calculator.domain.validateSpurInputs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Shared between SpurGearFormScreen and SpurGearReceiptScreen (both live in the same nested nav
 * graph, scoped to the same NavBackStackEntry) so the receipt survives back-navigation and
 * config changes without re-serializing the calculation result through nav args.
 */
class SpurGearViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val mn: StateFlow<String> = savedStateHandle.getStateFlow(KEY_MN, "1.00")
    val z: StateFlow<String> = savedStateHandle.getStateFlow(KEY_Z, "20")
    val alphaDeg: StateFlow<String> = savedStateHandle.getStateFlow(KEY_ALPHA, "20°00′00″00")
    val diameter: StateFlow<String> = savedStateHandle.getStateFlow(KEY_DIAMETER, DEFAULT_DIAMETER)
    val diameterOverride: StateFlow<Boolean> = savedStateHandle.getStateFlow(KEY_OVERRIDE, false)

    fun setMn(value: String) {
        savedStateHandle[KEY_MN] = value
        if (!diameterOverride.value) recalculateAutoDiameter(mnOverride = value)
    }

    fun setZ(value: String) {
        savedStateHandle[KEY_Z] = value
        if (!diameterOverride.value) recalculateAutoDiameter(zOverride = value)
    }

    fun setAlphaDeg(value: String) {
        savedStateHandle[KEY_ALPHA] = value
    }

    fun setDiameter(value: String) {
        savedStateHandle[KEY_OVERRIDE] = true
        savedStateHandle[KEY_DIAMETER] = value
    }

    fun resetToAutoDiameter() {
        savedStateHandle[KEY_OVERRIDE] = false
        recalculateAutoDiameter()
    }

    private fun recalculateAutoDiameter(mnOverride: String? = null, zOverride: String? = null) {
        val mnVal = parseLocaleDouble(mnOverride ?: mn.value) ?: 0.0
        val zVal = parseLocaleInt(zOverride ?: z.value) ?: 0
        val autoDiameter = (zVal + 2) * mnVal
        savedStateHandle[KEY_DIAMETER] = String.format(java.util.Locale.US, "%.2f", autoDiameter)
    }

    val errors: StateFlow<List<String>> = combine(mn, z, alphaDeg, diameter) { mnStr, zStr, alphaStr, diaStr ->
        val mnVal = parseLocaleDouble(mnStr)
        val zVal = parseLocaleInt(zStr)
        val alphaVal = parseReishauerAngle(alphaStr)
        val diaVal = parseLocaleDouble(diaStr)
        if (mnVal == null || zVal == null || alphaVal == null || diaVal == null) {
            emptyList()
        } else {
            validateSpurInputs(mnVal, zVal, diaVal)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val result: StateFlow<SpurGearResult?> = combine(mn, z, alphaDeg, diameter) { mnStr, zStr, alphaStr, diaStr ->
        val mnVal = parseLocaleDouble(mnStr)
        val zVal = parseLocaleInt(zStr)
        val alphaVal = parseReishauerAngle(alphaStr)
        val diaVal = parseLocaleDouble(diaStr)
        if (mnVal == null || zVal == null || alphaVal == null || diaVal == null) {
            null
        } else if (validateSpurInputs(mnVal, zVal, diaVal).isNotEmpty()) {
            null
        } else {
            computeSpurParams(mnVal, zVal, alphaVal, diaVal)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _combinations = MutableStateFlow<List<GearCombination>?>(null)
    val combinations: StateFlow<List<GearCombination>?> = _combinations.asStateFlow()
    private var combinationsJob: Job? = null
    private var combinationsForZ: Int? = null

    /** Runs the O(n^4) four-gear fallback search off the main thread; cancels a stale search
     * if the tooth count changes again before it finishes. */
    fun ensureCombinationsFor(z: Int) {
        if (combinationsForZ == z && _combinations.value != null) return
        combinationsJob?.cancel()
        combinationsForZ = z
        _combinations.value = null
        combinationsJob = viewModelScope.launch {
            val combos = withContext(Dispatchers.Default) { findSpurCombinations(z) }
            _combinations.value = combos
        }
    }

    companion object {
        // Matches the (z + 2) * mn auto-diameter formula for the default mn=1.0, z=20 inputs.
        private const val DEFAULT_DIAMETER = "22.00"
        private const val KEY_MN = "spur_mn"
        private const val KEY_Z = "spur_z"
        private const val KEY_ALPHA = "spur_alpha"
        private const val KEY_DIAMETER = "spur_diameter"
        private const val KEY_OVERRIDE = "spur_diameter_override"
    }
}
