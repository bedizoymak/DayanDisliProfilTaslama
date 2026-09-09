package com.dayandisli.calculator.ui.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayandisli.calculator.domain.GearCombination
import com.dayandisli.calculator.domain.HelicalGearResult
import com.dayandisli.calculator.domain.HelixDirection
import com.dayandisli.calculator.domain.computeHelicalParams
import com.dayandisli.calculator.domain.findHelicalCombinations
import com.dayandisli.calculator.domain.findTaksimatCombinations
import com.dayandisli.calculator.domain.parseLocaleDouble
import com.dayandisli.calculator.domain.parseLocaleInt
import com.dayandisli.calculator.domain.validateHelicalInputs
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

class HelicalGearViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    val mn: StateFlow<String> = savedStateHandle.getStateFlow(KEY_MN, "1.0")
    val z: StateFlow<String> = savedStateHandle.getStateFlow(KEY_Z, "20")
    val alphaDeg: StateFlow<String> = savedStateHandle.getStateFlow(KEY_ALPHA, "20.0")
    val betaDeg: StateFlow<String> = savedStateHandle.getStateFlow(KEY_BETA, "15.0")
    val helixDirection: StateFlow<HelixDirection> =
        savedStateHandle.getStateFlow(KEY_DIRECTION, HelixDirection.SAG.name)
            .map { HelixDirection.valueOf(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HelixDirection.SAG)

    fun setMn(value: String) { savedStateHandle[KEY_MN] = value }
    fun setZ(value: String) { savedStateHandle[KEY_Z] = value }
    fun setAlphaDeg(value: String) { savedStateHandle[KEY_ALPHA] = value }
    fun setBetaDeg(value: String) { savedStateHandle[KEY_BETA] = value }
    fun setHelixDirection(value: HelixDirection) { savedStateHandle[KEY_DIRECTION] = value.name }

    val errors: StateFlow<List<String>> = combine(mn, z, betaDeg) { mnStr, zStr, betaStr ->
        val mnVal = parseLocaleDouble(mnStr)
        val zVal = parseLocaleInt(zStr)
        val betaVal = parseLocaleDouble(betaStr)
        if (mnVal == null || zVal == null || betaVal == null) emptyList() else validateHelicalInputs(mnVal, zVal, betaVal)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val result: StateFlow<HelicalGearResult?> =
        combine(mn, z, alphaDeg, betaDeg, helixDirection) { mnStr, zStr, alphaStr, betaStr, direction ->
            val mnVal = parseLocaleDouble(mnStr)
            val zVal = parseLocaleInt(zStr)
            val alphaVal = parseLocaleDouble(alphaStr)
            val betaVal = parseLocaleDouble(betaStr)
            if (mnVal == null || zVal == null || alphaVal == null || betaVal == null) {
                null
            } else if (validateHelicalInputs(mnVal, zVal, betaVal).isNotEmpty()) {
                null
            } else {
                computeHelicalParams(mnVal, zVal, alphaVal, betaVal, direction)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _taksimatCombinations = MutableStateFlow<List<GearCombination>?>(null)
    val taksimatCombinations: StateFlow<List<GearCombination>?> = _taksimatCombinations.asStateFlow()

    private val _helicalCombinations = MutableStateFlow<List<GearCombination>?>(null)
    val helicalCombinations: StateFlow<List<GearCombination>?> = _helicalCombinations.asStateFlow()

    private var combinationsJob: Job? = null
    private var combinationsForKey: Pair<Int, Double>? = null

    fun ensureCombinationsFor(z: Int, helicalGearRatio: Double) {
        val key = z to helicalGearRatio
        if (combinationsForKey == key && _taksimatCombinations.value != null && _helicalCombinations.value != null) return
        combinationsJob?.cancel()
        combinationsForKey = key
        _taksimatCombinations.value = null
        _helicalCombinations.value = null
        combinationsJob = viewModelScope.launch {
            val taksimat = withContext(Dispatchers.Default) { findTaksimatCombinations(z) }
            _taksimatCombinations.value = taksimat
            val helical = withContext(Dispatchers.Default) { findHelicalCombinations(helicalGearRatio) }
            _helicalCombinations.value = helical
        }
    }

    companion object {
        private const val KEY_MN = "helical_mn"
        private const val KEY_Z = "helical_z"
        private const val KEY_ALPHA = "helical_alpha"
        private const val KEY_BETA = "helical_beta"
        private const val KEY_DIRECTION = "helical_direction"
    }
}
