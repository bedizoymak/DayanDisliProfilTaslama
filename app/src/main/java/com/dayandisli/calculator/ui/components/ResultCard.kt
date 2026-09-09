package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.domain.toFixedJs
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

/** A small metric card: quiet label on top, large bold tabular value below, optional unit.
 * Used throughout results/receipts as a 2-column grid so numeric values visually dominate their
 * labels. Reproduces the original ResultCard.tsx look with the app's typography scale. */
@Composable
fun ResultCard(label: String, value: Double, unit: String? = null, modifier: Modifier = Modifier) {
    ResultCardBase(label, value.toFixedJs(4), unit, modifier)
}

@Composable
fun ResultCard(label: String, value: String, unit: String? = null, modifier: Modifier = Modifier) {
    ResultCardBase(label, value, unit, modifier)
}

@Composable
private fun ResultCardBase(label: String, valueText: String, unit: String?, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(containerColor = AppColors.Slate800),
        border = BorderStroke(1.dp, AppColors.Slate700),
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(label, color = AppColors.Slate400, style = AppType.fieldLabel, maxLines = 1, overflow = TextOverflow.Ellipsis)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.xxs))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    valueText,
                    color = androidx.compose.ui.graphics.Color.White,
                    style = AppType.resultValueSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (unit != null) {
                    Text(
                        " $unit",
                        color = AppColors.Slate400,
                        style = AppType.supporting,
                        modifier = Modifier.padding(start = Spacing.xxs, bottom = 2.dp),
                    )
                }
            }
        }
    }
}

/** A large, single dominant result (e.g. total weight in kg) with an optional supporting caption
 * below (e.g. the same value in grams) and an accent color for the module. */
@Composable
fun PrimaryResultValue(
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    accentColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.White,
) {
    Column(modifier = modifier) {
        Text(value, color = accentColor, style = AppType.resultValueLarge)
        if (caption != null) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.xxs))
            Text(caption, color = AppColors.Slate500, style = AppType.supporting)
        }
    }
}
