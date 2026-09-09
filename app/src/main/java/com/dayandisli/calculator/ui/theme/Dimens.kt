package com.dayandisli.calculator.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Central spacing / sizing / radius scale for the app. Everything here follows a 4dp base grid.
 * Screens should reference these instead of scattering dp literals, so density and rhythm stay
 * consistent across the whole app.
 */
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object Radius {
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val pill = 999.dp
}

object Elevation {
    val flat = 0.dp
    val card = 1.dp
    val raised = 3.dp
}

object Dimens {
    /** Minimum touch target for nav destinations, icon buttons, dropdown triggers, chips. */
    val minTouchTarget = 48.dp

    /** Standard height for text fields / dropdown triggers so forms line up cleanly. */
    val fieldHeight = 56.dp

    /** Top bar height, consistent across the compact top app bar and wide layouts. */
    val topBarHeight = 64.dp

    /** Max content width for dense forms (Spur/Helical forms, Weight calculator). */
    val formMaxWidth = 720.dp

    /** Max content width for wide result / receipt layouts. */
    val receiptMaxWidth = 960.dp

    /** Standard horizontal screen padding on phones. */
    val screenPaddingHorizontal = 20.dp
    val screenPaddingVertical = 20.dp

    val iconSizeSmall = 18.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 32.dp

    val dividerThickness = 1.dp
    val borderThickness = 1.dp
}
