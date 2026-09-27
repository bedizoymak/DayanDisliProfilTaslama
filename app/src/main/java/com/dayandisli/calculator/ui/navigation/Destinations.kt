package com.dayandisli.calculator.ui.navigation

/** Route constants for the calculator's navigation graph. */
object Destinations {
    const val HOME = "home"
    const val MACHINES = "machines"
    const val MACHINE_DETAIL = "machines/{machineId}"
    const val WEIGHT = "weight"

    const val SPUR_FLOW = "spurFlow/{machineId}"
    const val SPUR_FORM = "spurFlow/{machineId}/form"
    const val SPUR_RECEIPT = "spurFlow/{machineId}/receipt"

    const val HELICAL_FLOW = "helicalFlow/{machineId}"
    const val HELICAL_FORM = "helicalFlow/{machineId}/form"
    const val HELICAL_RECEIPT = "helicalFlow/{machineId}/receipt"

    /** REISHAUER Taşlama has its own dedicated flow (not the generic Spur/Helical
     * MachineDetailScreen), scoped to machine id "reishauer-taslama". */
    const val REISHAUER_FLOW = "reishauerFlow/{machineId}"
    const val REISHAUER_FORM = "reishauerFlow/{machineId}/form"
    const val REISHAUER_RECEIPT = "reishauerFlow/{machineId}/receipt"
    const val REISHAUER_SPUR_FLOW = "reishauerSpurFlow/{machineId}"
    const val REISHAUER_SPUR_FORM = "reishauerSpurFlow/{machineId}/form"
    const val REISHAUER_SPUR_RECEIPT = "reishauerSpurFlow/{machineId}/receipt"

    fun machineDetail(machineId: String) = "machines/$machineId"
    fun spurFlow(machineId: String) = "spurFlow/$machineId"
    fun spurForm(machineId: String) = "spurFlow/$machineId/form"
    fun spurReceipt(machineId: String) = "spurFlow/$machineId/receipt"
    fun helicalFlow(machineId: String) = "helicalFlow/$machineId"
    fun helicalForm(machineId: String) = "helicalFlow/$machineId/form"
    fun helicalReceipt(machineId: String) = "helicalFlow/$machineId/receipt"
    fun reishauerFlow(machineId: String) = "reishauerFlow/$machineId"
    fun reishauerForm(machineId: String) = "reishauerFlow/$machineId/form"
    fun reishauerReceipt(machineId: String) = "reishauerFlow/$machineId/receipt"
    fun reishauerSpurForm(machineId: String) = "reishauerSpurFlow/$machineId/form"
    fun reishauerSpurReceipt(machineId: String) = "reishauerSpurFlow/$machineId/receipt"

    const val REISHAUER_MACHINE_ID = "reishauer-taslama"

    /** The three primary destinations preserved from CalculatorLayout.tsx's nav items. */
    val bottomNavItems = listOf(HOME, MACHINES, WEIGHT)
}
