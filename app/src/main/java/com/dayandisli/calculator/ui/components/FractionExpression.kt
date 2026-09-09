package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.domain.GearCombination
import com.dayandisli.calculator.domain.toDmscs
import com.dayandisli.calculator.domain.toFixedJs
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

/** A single stacked fraction (numerator / thin divider / denominator), centered, monospace. */
@Composable
private fun Fraction(
    numerator: String,
    denominator: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    italic: Boolean = false,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(numerator, color = textColor, style = AppType.resultValueSmall, fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal)
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(vertical = 2.dp).width(36.dp).height(2.dp).background(textColor),
        )
        Text(denominator, color = textColor, style = AppType.resultValueSmall, fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal)
    }
}

/**
 * Renders a [GearCombination] as a true mathematical equation: a restrained symbolic side using
 * italic variable names (A/B or A/B x C/D), a visible "=", and the actual selected tooth-count
 * values as stacked fractions in [accentColor]. Never renders a raw "a/b * c/d = 0.xxxx" string,
 * and never places the decimal ratio next to the equals sign — that belongs only in the metrics
 * section below (see [GearCombinationCard]).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FractionExpression(combination: GearCombination, accentColor: Color, modifier: Modifier = Modifier) {
    val c = combination.c
    val d = combination.d
    val isFourGear = c != null && d != null
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterVertically)) {
            Fraction("A", "B", AppColors.Slate300, italic = true)
            if (isFourGear) {
                Text(" × ", color = AppColors.Slate400, style = AppType.resultValueSmall, modifier = Modifier.padding(horizontal = Spacing.xs))
                Fraction("C", "D", AppColors.Slate300, italic = true)
            }
        }
        Text(
            " = ",
            color = AppColors.Slate300,
            style = AppType.resultValueSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterVertically).padding(horizontal = Spacing.sm),
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(Alignment.CenterVertically)) {
            Fraction(combination.a.toString(), combination.b.toString(), accentColor)
            if (isFourGear && c != null && d != null) {
                Text(" × ", color = AppColors.Slate400, style = AppType.resultValueSmall, modifier = Modifier.padding(horizontal = Spacing.xs))
                Fraction(c.toString(), d.toString(), accentColor)
            }
        }
    }
}

/**
 * A single gear-combination result card: index badge, dominant centered fraction expression,
 * a "2 Çark"/"4 Çark" badge, and secondary metrics (ratio / target / error / realized helix
 * angle). Replaces the old spreadsheet-style GearCombinationsTable rows.
 */
@Composable
fun GearCombinationCard(
    index: Int,
    combination: GearCombination,
    targetRatio: Double,
    accentColor: Color,
    modifier: Modifier = Modifier,
    realizedHelixAngleDeg: Double? = null,
    requestedHelixAngleDeg: Double? = null,
) {
    val isFourGear = combination.c != null && combination.d != null
    val error = kotlin.math.abs(combination.ratio - targetRatio)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = AppColors.Slate800),
        border = BorderStroke(1.dp, AppColors.Slate700),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "#${index.toString().padStart(2, '0')}",
                    color = AppColors.Slate500,
                    style = AppType.monoLabel,
                )
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(Radius.pill))
                        .padding(horizontal = Spacing.sm, vertical = 3.dp),
                ) {
                    Text(if (isFourGear) "4 Çark" else "2 Çark", color = accentColor, style = AppType.chip)
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
            FractionExpression(combination, accentColor, modifier = Modifier.fillMaxWidth())
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))

            MetricRow("Çark Oranı", combination.ratio.toFixedJs(6))
            MetricRow("Hedef Oran", targetRatio.toFixedJs(6))
            MetricRow("Hata", error.toFixedJs(9))
            if (realizedHelixAngleDeg != null) {
                MetricRow("Gerçekleşen Helis Açısı", realizedHelixAngleDeg.toDmscs())
                if (requestedHelixAngleDeg != null) {
                    MetricRow("Fark (β)", (realizedHelixAngleDeg - requestedHelixAngleDeg).toDmscs())
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = AppColors.Slate400, style = AppType.supporting)
        Text(value, color = Color.White, style = AppType.monoLabel, fontWeight = FontWeight.Medium)
    }
}

/** Group of [GearCombinationCard]s under a section heading, with an empty state matching the
 * previous table's. Used for both Spur/Helical and Reishauer receipts. */
@Composable
fun GearCombinationsSection(
    combinations: List<GearCombination>,
    title: String,
    targetRatio: Double,
    accentColor: Color,
    modifier: Modifier = Modifier,
    realizedHelixAngle: ((GearCombination) -> Double?)? = null,
    requestedHelixAngleDeg: Double? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = Color.White, style = AppType.cardTitle)
            if (combinations.isNotEmpty()) {
                Text("${combinations.size} sonuç", color = AppColors.Slate500, style = AppType.supporting)
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))

        if (combinations.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(containerColor = AppColors.Slate800),
                border = BorderStroke(1.dp, AppColors.Slate700),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Filled.SearchOff, contentDescription = null, tint = AppColors.Slate600, modifier = Modifier.padding(bottom = Spacing.xs))
                    Text("Uygun kombinasyon bulunamadı.", color = AppColors.Slate400, style = AppType.body)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                combinations.forEachIndexed { i, combo ->
                    GearCombinationCard(
                        index = i + 1,
                        combination = combo,
                        targetRatio = targetRatio,
                        accentColor = accentColor,
                        realizedHelixAngleDeg = realizedHelixAngle?.invoke(combo),
                        requestedHelixAngleDeg = requestedHelixAngleDeg,
                    )
                }
            }
        }
    }
}
