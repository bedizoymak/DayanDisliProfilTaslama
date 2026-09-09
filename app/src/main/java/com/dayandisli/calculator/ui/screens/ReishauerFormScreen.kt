package com.dayandisli.calculator.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayandisli.calculator.R
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

/** REISHAUER Taşlama's dedicated form (distinct from the generic Spur/Helical
 * MachineDetailScreen flow), using the D,M,S,CS angle format. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReishauerFormScreen(machineId: String, viewModel: ReishauerViewModel = viewModel(), onViewReceipt: () -> Unit) {
    val machine = getMachineById(machineId)
    val mn by viewModel.mn.collectAsStateWithLifecycle()
    val z by viewModel.z.collectAsStateWithLifecycle()
    val alphaRaw by viewModel.alphaRaw.collectAsStateWithLifecycle()
    val betaRaw by viewModel.betaRaw.collectAsStateWithLifecycle()
    val diameterOverride by viewModel.diameterOverride.collectAsStateWithLifecycle()
    val autoDiameter by viewModel.autoDiameter.collectAsStateWithLifecycle()
    val helixDirection by viewModel.helixDirection.collectAsStateWithLifecycle()
    val errors by viewModel.errors.collectAsStateWithLifecycle()
    val result by viewModel.result.collectAsStateWithLifecycle()

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ContentContainer(
            maxWidth = Dimens.formMaxWidth,
            modifier = Modifier.padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical).fillMaxWidth(),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxWidth().aspectRatio(21f / 9f).clip(RoundedCornerShape(Radius.lg)),
            ) {
                Image(
                    painter = painterResource(R.drawable.machine_reishauer_taslama),
                    contentDescription = "REISHAUER Taşlama makinesi",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(21f / 9f),
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))

            ScreenHeader(
                eyebrow = machine?.name ?: "Makine",
                title = "Reishauer Helis Dişli Taşlama",
                subtitle = "D,M,S,CS formatında açı girişi ile profil taşlama hesaplaması",
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            SectionCard(title = "Dişli Parametreleri", icon = Icons.Filled.PrecisionManufacturing, accent = ModuleAccent.Helical, modifier = Modifier.fillMaxWidth()) {
                EngineeringTextField("Modül (mn)", mn, viewModel::setMn, unit = "mm")
                EngineeringTextField("Diş Sayısı (Z)", z, viewModel::setZ, unit = "diş", keyboardType = KeyboardType.Number)
                EngineeringTextField(
                    "Kavrama Açısı (α)",
                    alphaRaw,
                    viewModel::setAlphaRaw,
                    unit = "D,M,S,CS",
                    keyboardType = KeyboardType.Text,
                    supportingText = "örn. 20,30,45,25",
                )
                EngineeringTextField(
                    "Helis Açısı (β)",
                    betaRaw,
                    viewModel::setBetaRaw,
                    unit = "D,M,S,CS",
                    keyboardType = KeyboardType.Text,
                    supportingText = "örn. 15,20",
                )

                EngineeringDropdown(
                    label = "Helis Yönü",
                    options = HelixDirection.entries,
                    selected = helixDirection,
                    optionLabel = { it.label },
                    onSelected = viewModel::setHelixDirection,
                )

                if (autoDiameter != null) {
                    Text(
                        "Otomatik Hesaplanan Çap: ${autoDiameter!!.toFixedJs(4)} mm",
                        color = AppColors.Slate300,
                        style = AppType.body,
                        modifier = Modifier.padding(bottom = Spacing.xs),
                    )
                }
                EngineeringTextField(
                    "Çap (ø) — manuel geçersiz kılma",
                    diameterOverride,
                    viewModel::setDiameterOverride,
                    unit = "mm",
                    supportingText = "boş bırakılırsa otomatik çap kullanılır",
                )

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

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            SectionCard(title = "Hesaplama Sonuçları", modifier = Modifier.fillMaxWidth()) {
                if (result != null) {
                    val r = result!!
                    val cells: List<Pair<String, String>> = listOf(
                        "Modül (mn)" to r.mn.toFixedJs(4),
                        "Diş Sayısı (Z)" to r.z.toString(),
                        "Çap (ø)" to "${r.diameter.toFixedJs(2)} mm",
                        "Nominal Çap" to "${r.nominalDiameter.toFixedJs(2)} mm",
                        "Wk (k=${r.k1})" to "${r.wk1.toFixedJs(4)} mm",
                        "Wk (k=${r.k2})" to "${r.wk2.toFixedJs(4)} mm",
                        "Wk (k=${r.k3})" to "${r.wk3.toFixedJs(4)} mm",
                        "Taksimat Oranı" to r.taksimatRatio.toFixedJs(6),
                        "Helis Çark Oranı" to r.helixGearRatio.toFixedJs(9),
                    )
                    cells.chunked(2).forEach { rowCells ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            rowCells.forEach { (label, value) ->
                                ResultCard(label = label, value = value, modifier = Modifier.weight(1f))
                            }
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
                    }
                } else {
                    Text("Geçerli parametreler girin", color = AppColors.Slate400, style = AppType.body)
                }
            }

            if (result != null) {
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
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))
        }
    }
}
