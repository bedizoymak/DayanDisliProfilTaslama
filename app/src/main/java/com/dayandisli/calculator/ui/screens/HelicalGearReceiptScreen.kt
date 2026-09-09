package com.dayandisli.calculator.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayandisli.calculator.domain.getMachineById
import com.dayandisli.calculator.domain.toFixedJs
import com.dayandisli.calculator.pdf.PdfReceiptGenerator
import com.dayandisli.calculator.ui.components.ContentContainer
import com.dayandisli.calculator.ui.components.GearCombinationsSection
import com.dayandisli.calculator.ui.components.ReceiptActionBar
import com.dayandisli.calculator.ui.components.ResultCard
import com.dayandisli.calculator.ui.components.ScreenHeader
import com.dayandisli.calculator.ui.components.SectionCard
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Dimens
import com.dayandisli.calculator.ui.theme.ModuleAccent
import com.dayandisli.calculator.ui.theme.Spacing

/** Reproduces pages/HelicalGearReceipt.tsx. */
@Composable
fun HelicalGearReceiptScreen(machineId: String, viewModel: HelicalGearViewModel = viewModel()) {
    val machine = getMachineById(machineId)
    val result by viewModel.result.collectAsStateWithLifecycle()
    val taksimatCombinations by viewModel.taksimatCombinations.collectAsStateWithLifecycle()
    val helicalCombinations by viewModel.helicalCombinations.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val r = result
    if (r == null || machine == null) {
        Column(modifier = Modifier.padding(Dimens.screenPaddingHorizontal)) {
            Text("Hesaplama sonucu bulunamadı.", color = AppColors.Slate400)
        }
        return
    }

    LaunchedEffect(r.z, r.helicalGearRatio) { viewModel.ensureCombinationsFor(r.z, r.helicalGearRatio) }

    val fileName = "helis-disli-recete-z${r.z}"
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            val bytes = PdfReceiptGenerator.renderHelicalReceiptPdf(
                context, machine, r, taksimatCombinations ?: emptyList(), helicalCombinations ?: emptyList(),
            )
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
        }
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ContentContainer(
            maxWidth = Dimens.receiptMaxWidth,
            modifier = Modifier.padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical).fillMaxWidth(),
        ) {
            ScreenHeader(eyebrow = machine.name, title = "Helisel Dişli Üretim Reçetesi", subtitle = "Taksimat ve helis çark kombinasyonları dahil")
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            ReceiptActionBar(
                accent = ModuleAccent.Helical,
                onShare = {
                    val bytes = PdfReceiptGenerator.renderHelicalReceiptPdf(
                        context, machine, r, taksimatCombinations ?: emptyList(), helicalCombinations ?: emptyList(),
                    )
                    PdfReceiptGenerator.shareBytes(context, bytes, fileName)
                },
                onSave = { saveLauncher.launch("$fileName.pdf") },
                onPrint = {
                    val bytes = PdfReceiptGenerator.renderHelicalReceiptPdf(
                        context, machine, r, taksimatCombinations ?: emptyList(), helicalCombinations ?: emptyList(),
                    )
                    PdfReceiptGenerator.printBytes(context, bytes, fileName)
                },
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            SectionCard(title = "Parametreler", icon = Icons.Filled.Description, accent = ModuleAccent.Helical, modifier = Modifier.fillMaxWidth()) {
                val cells = listOf(
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        rowCells.forEach { (label, value) ->
                            Column(modifier = Modifier.weight(1f)) {
                                Text(label, color = AppColors.Slate400, style = AppType.supporting)
                                Text(value, color = Color.White, style = AppType.monoLabel)
                            }
                        }
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            if (taksimatCombinations == null || helicalCombinations == null) {
                CalculatingIndicator()
            } else {
                GearCombinationsSection(
                    combinations = taksimatCombinations!!,
                    title = "Taksimat Çark Kombinasyonları",
                    targetRatio = r.taksimatRatio,
                    accentColor = AppColors.Blue400,
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                GearCombinationsSection(
                    combinations = helicalCombinations!!,
                    title = "Helis Çark Kombinasyonları",
                    targetRatio = r.helicalGearRatio,
                    accentColor = AppColors.Emerald400,
                    realizedHelixAngle = { combo -> com.dayandisli.calculator.domain.helicalActualHelixAngleDeg(r.mn, combo.ratio) },
                    requestedHelixAngleDeg = r.betaDeg,
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 80.dp))
        }
    }
}
