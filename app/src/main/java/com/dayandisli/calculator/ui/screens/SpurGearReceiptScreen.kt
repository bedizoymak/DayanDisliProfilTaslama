package com.dayandisli.calculator.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayandisli.calculator.domain.getMachineById
import com.dayandisli.calculator.domain.toFixedJs
import com.dayandisli.calculator.domain.toDmscs
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

/** Reproduces pages/SpurGearReceipt.tsx: a production-report-style preview (Parameters →
 * Calculated Values → Gear Combinations → Actions), echoing the PDF hierarchy without rendering
 * the PDF itself. PDF export continues to go through [PdfReceiptGenerator], unchanged. */
@Composable
fun SpurGearReceiptScreen(machineId: String, viewModel: SpurGearViewModel = viewModel()) {
    val machine = getMachineById(machineId)
    val result by viewModel.result.collectAsStateWithLifecycle()
    val combinations by viewModel.combinations.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val r = result
    if (r == null || machine == null) {
        Column(modifier = Modifier.padding(Dimens.screenPaddingHorizontal)) {
            Text("Hesaplama sonucu bulunamadı.", color = AppColors.Slate400)
        }
        return
    }

    LaunchedEffect(r.z) { viewModel.ensureCombinationsFor(r.z) }

    val fileName = "duz-disli-recete-z${r.z}"
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            val bytes = PdfReceiptGenerator.renderSpurReceiptPdf(context, machine, r, combinations ?: emptyList())
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
        }
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ContentContainer(
            maxWidth = Dimens.receiptMaxWidth,
            modifier = Modifier.padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical).fillMaxWidth(),
        ) {
            ScreenHeader(eyebrow = machine.name, title = "Düz Dişli Üretim Reçetesi", subtitle = "Wk taksimat kombinasyonları dahil")
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            ReceiptActionBar(
                accent = ModuleAccent.Spur,
                onShare = {
                    val bytes = PdfReceiptGenerator.renderSpurReceiptPdf(context, machine, r, combinations ?: emptyList())
                    PdfReceiptGenerator.shareBytes(context, bytes, fileName)
                },
                onSave = { saveLauncher.launch("$fileName.pdf") },
                onPrint = {
                    val bytes = PdfReceiptGenerator.renderSpurReceiptPdf(context, machine, r, combinations ?: emptyList())
                    PdfReceiptGenerator.printBytes(context, bytes, fileName)
                },
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            SectionCard(title = "Parametreler", icon = Icons.Filled.Description, accent = ModuleAccent.Spur, modifier = Modifier.fillMaxWidth()) {
                val cells = listOf(
                    "Modül (mn)" to "${r.mn.toFixedJs(2)}",
                    "Diş Sayısı (Z)" to r.z.toString(),
                    "Kavrama Açısı (α)" to r.alphaDeg.toDmscs().replace(" ", ""),
                    "Çap (ø)" to "${r.diameter.toFixedJs(2)} mm",
                    "Wk (k=${r.kFloor})" to "${r.wkFloor.toFixedJs(4)} mm",
                    "Wk (k=${r.kCeil})" to "${r.wkCeil.toFixedJs(4)} mm",
                    "Wk (k=${r.kThird})" to "${r.wkThird.toFixedJs(4)} mm",
                    "Taksimat Oranı" to r.taksimatRatio.toFixedJs(9),
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

            if (combinations == null) {
                CalculatingIndicator()
            } else {
                GearCombinationsSection(
                    combinations = combinations!!,
                    title = "Taksimat Çark Kombinasyonları",
                    targetRatio = r.taksimatRatio,
                    accentColor = AppColors.Blue400,
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 80.dp))
        }
    }
}

@Composable
internal fun CalculatingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = Spacing.sm)) {
        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = AppColors.Blue400)
        Text(
            "Kombinasyonlar hesaplanıyor…",
            color = AppColors.Slate400,
            style = AppType.body,
            modifier = Modifier.padding(start = Spacing.sm),
        )
    }
}
