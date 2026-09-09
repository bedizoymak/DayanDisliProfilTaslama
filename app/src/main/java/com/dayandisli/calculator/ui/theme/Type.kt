package com.dayandisli.calculator.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography scale for the industrial/engineering look: bold screen titles, semibold section
 * headings, large tabular-numeral result values, and quiet supporting/label text. Kept separate
 * from Material's default Typography so numeric-heavy result surfaces get a consistent monospace
 * treatment without touching the app-wide MaterialTheme.typography (which several default
 * components still rely on).
 */
object AppType {
    val screenTitle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp)
    val screenSubtitle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)

    val sectionHeading = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

    val resultValueLarge = TextStyle(
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.sp,
    )
    val resultValueMedium = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
    )
    val resultValueSmall = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.Monospace,
    )

    val fieldLabel = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
    val supporting = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)
    val body = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)

    val cardTitle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    val chip = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
    val monoLabel = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
}
