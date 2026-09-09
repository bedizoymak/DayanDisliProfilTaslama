package com.dayandisli.calculator.ui.theme

import androidx.compose.ui.graphics.Color

/** Tailwind default palette hexes used by the web calculator's utility classes. */
object AppColors {
    val Slate950 = Color(0xFF020617)
    val Slate900 = Color(0xFF0F172A)
    val Slate800 = Color(0xFF1E293B)
    val Slate700 = Color(0xFF334155)
    val Slate600 = Color(0xFF475569)
    val Slate500 = Color(0xFF64748B)
    val Slate400 = Color(0xFF94A3B8)
    val Slate300 = Color(0xFFCBD5E1)

    val Blue700 = Color(0xFF1D4ED8)
    val Blue600 = Color(0xFF2563EB)
    val Blue500 = Color(0xFF3B82F6)
    val Blue400 = Color(0xFF60A5FA)

    val Emerald900 = Color(0xFF064E3B)
    val Emerald700 = Color(0xFF047857)
    val Emerald600 = Color(0xFF059669)
    val Emerald400 = Color(0xFF34D399)

    val Purple700 = Color(0xFF7E22CE)
    val Purple600 = Color(0xFF9333EA)
    val Purple400 = Color(0xFFC084FC)

    val Amber900 = Color(0xFF78350F)
    val Amber700 = Color(0xFFB45309)
    val Amber400 = Color(0xFFFBBF24)

    val Red900 = Color(0xFF7F1D1D)
    val Red800 = Color(0xFF991B1B)
    val Red400 = Color(0xFFF87171)

    val White = Color(0xFFFFFFFF)
}

/** Per-module accent colors, kept controlled and consistent across cards/icons/CTAs/borders. */
enum class ModuleAccent(val strong: Color, val soft: Color, val onSoft: Color) {
    Spur(AppColors.Blue600, AppColors.Blue600.copy(alpha = 0.16f), AppColors.Blue400),
    Helical(AppColors.Purple600, AppColors.Purple600.copy(alpha = 0.16f), AppColors.Purple400),
    Weight(AppColors.Emerald600, AppColors.Emerald600.copy(alpha = 0.16f), AppColors.Emerald400),
    Neutral(AppColors.Slate600, AppColors.Slate600.copy(alpha = 0.16f), AppColors.Slate300),
    Warning(AppColors.Amber700, AppColors.Amber700.copy(alpha = 0.18f), AppColors.Amber400),
    Danger(AppColors.Red800, AppColors.Red800.copy(alpha = 0.18f), AppColors.Red400),
}
