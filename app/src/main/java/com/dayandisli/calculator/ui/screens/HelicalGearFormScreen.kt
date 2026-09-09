package com.dayandisli.calculator.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayandisli.calculator.domain.HelixDirection
import com.dayandisli.calculator.domain.getMachineById
import com.dayandisli.calculator.domain.toFixedJs
import com.dayandisli.calculator.ui.components.ContentContainer
import com.dayandisli.calculator.ui.components.EngineeringDropdown
import com.dayandisli.calculator.ui.components.EngineeringTextField
import com.dayandisli.calculator.ui.components.ResultCard
import com.dayandisli.calculator.ui.components.ScreenHeader
import com.dayandisli.calculator.ui.components.SectionCard
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Dimens
import com.dayandisli.calculator.ui.theme.ModuleAccent
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

/** Reproduces pages/HelicalGearForm.tsx. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelicalGearFormScreen(machineId: String, viewModel: HelicalGearViewModel = viewModel(), onViewReceipt: () -> Unit) {
    val machine = getMachineById(machineId)
    val mn by viewModel.mn.collectAsStateWithLifecycle()
    val z by viewModel.z.collectAsStateWithLifecycle()
    val alphaDeg by viewModel.alphaDeg.collectAsStateWithLifecycle()
    val betaDeg by viewModel.betaDeg.collectAsStateWithLifecycle()
    val helixDirection by viewModel.helixDirection.collectAsStateWithLifecycle()
    val errors by viewModel.errors.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ContentContainer(
            maxWidth = Dimens.formMaxWidth,
            modifier = Modifier.padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical).fillMaxWidth(),
        ) {
            ScreenHeader(
                eyebrow = machine?.name ?: "Makine",
                title = "Helis Dişli Hesaplama",
                subtitle = "Helical gear parametrelerini girin",
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            SectionCard(title = "Dişli Parametreleri", icon = Icons.Filled.Sync, accent = ModuleAccent.Helical, modifier = Modifier.fillMaxWidth()) {
                EngineeringTextField("Modül (mn)", mn, viewModel::setMn, unit = "mm")
                EngineeringTextField("Diş Sayısı (Z)", z, viewModel::setZ, unit = "diş", keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                EngineeringTextField("Kavrama Açısı (α)", alphaDeg, viewModel::setAlphaDeg, unit = "°")
                EngineeringTextField("Helis Açısı (β)", betaDeg, viewModel::setBetaDeg, unit = "°")

                HelixDirectionSelector(
                    selected = helixDirection,
                    onSelected = viewModel::setHelixDirection,
                    accent = ModuleAccent.Helical,
                )

                if (result != null) {
                    Text(
                        "Hesaplanan Çap (ø): ${String.format(java.util.Locale.US, "%.2f", result!!.diameter)} mm",
                        color = AppColors.Slate300,
                        style = AppType.body,
                        modifier = Modifier.padding(bottom = Spacing.sm),
                    )
                }

                if (errors.isNotEmpty()) {
                    Column {
                        errors.forEach { error ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = AppColors.Red400, modifier = Modifier.padding(end = 4.dp))
                                Text(error, color = AppColors.Red400, style = AppType.body)
                            }
                        }
                    }
                }
            }

            if (result != null) {
                val r = result!!
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

                SectionCard(title = "Hesaplama Sonuçları", modifier = Modifier.fillMaxWidth()) {
                    val cells: List<Pair<String, String>> = listOf(
                        "Modül (mn)" to r.mn.toFixedJs(4),
                        "Diş Sayısı (Z)" to r.z.toString(),
                        "Kavrama Açısı (α)" to "${r.alphaDeg.toFixedJs(2)}°",
                        "Helis Açısı (β)" to "${r.betaDeg.toFixedJs(2)}°",
                        "Helis Yönü" to r.helixDirection.label,
                        "Çap (ø)" to "${r.diameter.toFixedJs(2)} mm",
                        "Wk (k=${r.kFloor})" to "${r.wkFloor.toFixedJs(4)} mm",
                        "Wk (k=${r.kCeil})" to "${r.wkCeil.toFixedJs(4)} mm",
                        "Taksimat Oranı" to r.taksimatRatio.toFixedJs(6),
                        "Helis Çark Oranı" to r.helicalGearRatio.toFixedJs(6),
                    )
                    cells.chunked(2).forEach { rowCells ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            rowCells.forEach { (label, value) ->
                                ResultCard(label = label, value = value, modifier = Modifier.weight(1f))
                            }
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
                    }
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))
                Button(
                    onClick = onViewReceipt,
                    modifier = Modifier.fillMaxWidth().height(Dimens.fieldHeight),
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple600),
                ) {
                    Text("Reçeteyi Görüntüle (PDF)", style = MaterialTheme.typography.titleSmall)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = Spacing.xs))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 80.dp))
        }
    }
}

@Composable
fun HelixDirectionSelector(
    selected: HelixDirection,
    onSelected: (HelixDirection) -> Unit,
    accent: ModuleAccent,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(bottom = Spacing.md)) {
        Text("Helis Yönü", style = AppType.fieldLabel, color = AppColors.Slate300)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
            HelixDirection.entries.forEach { dir ->
                val isSelected = dir == selected
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimens.minTouchTarget)
                        .clickable { onSelected(dir) },
                    shape = RoundedCornerShape(Radius.md),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) accent.strong.copy(alpha = 0.35f) else AppColors.Slate900.copy(alpha = 0.5f),
                    ),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) accent.onSoft else AppColors.Slate700,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(Dimens.minTouchTarget),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = dir.label,
                            color = if (isSelected) Color.White else AppColors.Slate400,
                            style = AppType.fieldLabel,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
