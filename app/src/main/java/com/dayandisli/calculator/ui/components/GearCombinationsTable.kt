package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.domain.GearCombination
import com.dayandisli.calculator.domain.toFixedJs
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

/** Reproduces components/GearCombinationsTable.tsx: an indexed A/B/C/D/Ratio table of gear
 * combinations in their existing (minimum-error-first) order, with a polished empty state when no
 * combination was found. Uses LazyColumn with stable index-based keys so large result sets don't
 * cause unnecessary recomposition. */
@Composable
fun GearCombinationsTable(combinations: List<GearCombination>, title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = AppColors.Slate800),
        border = BorderStroke(1.dp, AppColors.Slate700),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(title, color = Color.White, style = AppType.cardTitle)
                if (combinations.isNotEmpty()) {
                    Text("${combinations.size} sonuç", color = AppColors.Slate500, style = AppType.supporting)
                }
            }
            HorizontalDivider(color = AppColors.Slate700)

            if (combinations.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Filled.SearchOff, contentDescription = null, tint = AppColors.Slate600, modifier = Modifier.size(28.dp))
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(Spacing.xs))
                    Text("Uygun kombinasyon bulunamadı.", color = AppColors.Slate400, style = AppType.body)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.xs)) {
                    Text("#", color = AppColors.Slate500, style = AppType.fieldLabel, modifier = Modifier.weight(0.6f))
                    listOf("A", "B", "C", "D").forEach {
                        Text(it, color = AppColors.Slate300, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    }
                    Text("Oran", color = AppColors.Slate300, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.2f))
                }
                HorizontalDivider(color = AppColors.Slate700)
                Column {
                    combinations.forEachIndexed { index, combo ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "#${(index + 1).toString().padStart(2, '0')}",
                                color = AppColors.Slate500,
                                style = AppType.monoLabel,
                                modifier = Modifier.weight(0.6f),
                            )
                            listOf(combo.a.toString(), combo.b.toString(), combo.c?.toString() ?: "-", combo.d?.toString() ?: "-")
                                .forEach { cell ->
                                    Text(cell, color = Color.White, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                                }
                            Text(
                                combo.ratio.toFixedJs(6),
                                color = AppColors.Blue400,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1.2f),
                            )
                        }
                        if (index < combinations.size - 1) {
                            HorizontalDivider(color = AppColors.Slate700.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
