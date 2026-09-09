package com.dayandisli.calculator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayandisli.calculator.domain.getMachineById
import com.dayandisli.calculator.ui.components.ContentContainer
import com.dayandisli.calculator.ui.components.EngineeringTextField
import com.dayandisli.calculator.ui.components.ResultCard
import com.dayandisli.calculator.ui.components.ScreenHeader
import com.dayandisli.calculator.ui.components.SectionCard
import com.dayandisli.calculator.ui.components.engineeringFieldColors
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Dimens
import com.dayandisli.calculator.ui.theme.ModuleAccent
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

/** Reproduces pages/SpurGearForm.tsx. */
@Composable
fun SpurGearFormScreen(machineId: String, viewModel: SpurGearViewModel = viewModel(), onViewReceipt: () -> Unit) {
    val machine = getMachineById(machineId)
    val mn by viewModel.mn.collectAsStateWithLifecycle()
    val z by viewModel.z.collectAsStateWithLifecycle()
    val alphaDeg by viewModel.alphaDeg.collectAsStateWithLifecycle()
    val diameter by viewModel.diameter.collectAsStateWithLifecycle()
    val diameterOverride by viewModel.diameterOverride.collectAsStateWithLifecycle()
    val errors by viewModel.errors.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ContentContainer(
            maxWidth = Dimens.formMaxWidth,
            modifier = Modifier.padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical).fillMaxWidth(),
        ) {
            ScreenHeader(
                eyebrow = machine?.name ?: "Makine",
                title = "Düz Dişli Hesaplama",
                subtitle = "Spur gear parametrelerini girin",
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            SectionCard(title = "Dişli Parametreleri", icon = Icons.Filled.Settings, accent = ModuleAccent.Spur, modifier = Modifier.fillMaxWidth()) {
                EngineeringTextField("Modül (mn)", mn, viewModel::setMn, unit = "mm")
                EngineeringTextField("Diş Sayısı (Z)", z, viewModel::setZ, unit = "diş", keyboardType = KeyboardType.Number)
                EngineeringTextField("Kavrama Açısı (α)", alphaDeg, viewModel::setAlphaDeg, unit = "°")
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Çap (ø)", style = AppType.fieldLabel, color = AppColors.Slate300)
                        if (!diameterOverride) {
                            Text(" (otomatik hesaplanan)", style = AppType.supporting, color = AppColors.Blue400)
                        }
                    }
                    OutlinedTextField(
                        value = diameter,
                        onValueChange = viewModel::setDiameter,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = engineeringFieldColors(),
                        singleLine = true,
                        suffix = { Text("mm", style = AppType.supporting, color = AppColors.Slate400) },
                    )
                    if (diameterOverride) {
                        TextButton(onClick = viewModel::resetToAutoDiameter) {
                            Text("Otomatik hesaplamaya dön", color = AppColors.Blue400, style = AppType.supporting)
                        }
                    }
                }

                if (errors.isNotEmpty()) {
                    Column(modifier = Modifier.padding(top = Spacing.sm)) {
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
                    val cells = listOf(
                        "Modül (mn)" to (r.mn to null),
                        "Diş Sayısı (Z)" to (r.z.toDouble() to null),
                        "Kavrama Açısı (α)" to (r.alphaDeg to "°"),
                        "Çap (ø)" to (r.diameter to "mm"),
                        "Wk (k=${r.kFloor})" to (r.wkFloor to "mm"),
                        "Wk (k=${r.kCeil})" to (r.wkCeil to "mm"),
                        "Taksimat Oranı" to (r.taksimatRatio to null),
                        "k değeri" to (r.k to null),
                    )
                    cells.chunked(2).forEach { rowCells ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            rowCells.forEach { (label, pair) ->
                                ResultCard(label = label, value = pair.first, unit = pair.second, modifier = Modifier.weight(1f))
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
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue600),
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
