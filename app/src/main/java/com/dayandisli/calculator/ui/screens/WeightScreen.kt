package com.dayandisli.calculator.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Scale
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dayandisli.calculator.domain.GeometryType
import com.dayandisli.calculator.domain.MaterialOption
import com.dayandisli.calculator.domain.toFixedJs
import com.dayandisli.calculator.ui.components.ContentContainer
import com.dayandisli.calculator.ui.components.EngineeringDropdown
import com.dayandisli.calculator.ui.components.EngineeringTextField
import com.dayandisli.calculator.ui.components.PrimaryResultValue
import com.dayandisli.calculator.ui.components.ScreenHeader
import com.dayandisli.calculator.ui.components.SectionCard
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Dimens
import com.dayandisli.calculator.ui.theme.ModuleAccent
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Reproduces pages/WeightCalculation.tsx: geometry/material selection, a dominant weight result
 * card, and a visually separated cost card. Emerald accent, kept restrained (not a fully green
 * screen). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(viewModel: WeightViewModel = viewModel()) {
    val geometry by viewModel.geometry.collectAsStateWithLifecycle()
    val diameter by viewModel.diameter.collectAsStateWithLifecycle()
    val length by viewModel.length.collectAsStateWithLifecycle()
    val outerDiameter by viewModel.outerDiameter.collectAsStateWithLifecycle()
    val innerDiameter by viewModel.innerDiameter.collectAsStateWithLifecycle()
    val width by viewModel.width.collectAsStateWithLifecycle()
    val height by viewModel.height.collectAsStateWithLifecycle()
    val material by viewModel.material.collectAsStateWithLifecycle()
    val customDensity by viewModel.customDensity.collectAsStateWithLifecycle()
    val pricePerKg by viewModel.pricePerKg.collectAsStateWithLifecycle()
    val quantity by viewModel.quantity.collectAsStateWithLifecycle()
    val quantityValue by viewModel.quantityValue.collectAsStateWithLifecycle()
    val currentDensity by viewModel.currentDensity.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cost by viewModel.cost.collectAsStateWithLifecycle()

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ContentContainer(
            maxWidth = Dimens.formMaxWidth,
            modifier = Modifier.padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical).fillMaxWidth(),
        ) {
            ScreenHeader(
                title = "Ağırlık Hesaplama",
                subtitle = "Farklı geometriler için tahmini ağırlık ve maliyet hesaplama",
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            SectionCard(title = "Geometri ve Malzeme", icon = Icons.Filled.Scale, accent = ModuleAccent.Weight, modifier = Modifier.fillMaxWidth()) {
                EngineeringDropdown(
                    label = "Geometri Tipi",
                    options = GeometryType.entries,
                    selected = geometry,
                    optionLabel = { it.label },
                    onSelected = viewModel::setGeometry,
                )

                when (geometry) {
                    GeometryType.SOLID -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            EngineeringTextField("Çap", diameter, viewModel::setDiameter, unit = "mm", modifier = Modifier.weight(1f))
                            EngineeringTextField("Uzunluk", length, viewModel::setLength, unit = "mm", modifier = Modifier.weight(1f))
                        }
                    }
                    GeometryType.PIPE -> {
                        EngineeringTextField("Dış Çap", outerDiameter, viewModel::setOuterDiameter, unit = "mm")
                        EngineeringTextField("İç Çap", innerDiameter, viewModel::setInnerDiameter, unit = "mm")
                        EngineeringTextField("Uzunluk", length, viewModel::setLength, unit = "mm")
                    }
                    GeometryType.FLAT -> {
                        EngineeringTextField("Genişlik", width, viewModel::setWidth, unit = "mm")
                        EngineeringTextField("Yükseklik", height, viewModel::setHeight, unit = "mm")
                        EngineeringTextField("Uzunluk", length, viewModel::setLength, unit = "mm")
                    }
                }

                EngineeringDropdown(
                    label = "Malzeme",
                    options = MaterialOption.entries,
                    selected = material,
                    optionLabel = { "${it.label} (${it.density} g/cm³)" },
                    onSelected = viewModel::setMaterial,
                )

                if (material == MaterialOption.CUSTOM) {
                    EngineeringTextField("Özel Yoğunluk", customDensity, viewModel::setCustomDensity, unit = "g/cm³")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    EngineeringTextField(
                        "Malzeme Fiyatı",
                        pricePerKg,
                        viewModel::setPricePerKg,
                        unit = "TL/kg",
                        modifier = Modifier.weight(1f),
                    )
                    EngineeringTextField(
                        "Adet",
                        quantity,
                        viewModel::setQuantity,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f),
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.Slate900.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, AppColors.Slate600),
                    shape = RoundedCornerShape(Radius.sm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text("Kullanılan Yoğunluk", color = AppColors.Slate400, style = AppType.fieldLabel)
                        Text("${currentDensity.toFixedJs(2)} g/cm³", color = Color.White, style = AppType.monoLabel)
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(containerColor = AppColors.Emerald900.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, AppColors.Emerald700.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Inventory2, contentDescription = null, tint = AppColors.Emerald400)
                        Text(" Ağırlık", color = Color.White, style = AppType.sectionHeading)
                    }
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))

                    val err = state.validationError
                    val result = state.result
                    when {
                        err != null -> Text(err, color = AppColors.Red400, style = AppType.body)
                        result != null -> {
                            Text("Birim Ağırlık", color = AppColors.Slate400, style = AppType.fieldLabel)
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 2.dp))
                            PrimaryResultValue(
                                value = "${result.weightKg.toFixedJs(2)} kg",
                                caption = "${result.weightGrams.toFixedJs(1)} gram",
                            )

                            if (quantityValue > 1) {
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                                androidx.compose.material3.HorizontalDivider(color = AppColors.Emerald700.copy(alpha = 0.3f))
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                                Text(
                                    "Toplam Ağırlık ($quantityValue Adet)",
                                    color = AppColors.Emerald400,
                                    style = AppType.fieldLabel,
                                )
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 2.dp))
                                PrimaryResultValue(
                                    value = "${result.totalWeightKg.toFixedJs(2)} kg",
                                    caption = "${result.totalWeightGrams.toFixedJs(1)} gram",
                                    accentColor = AppColors.Emerald400,
                                )
                            }
                        }
                        else -> Text("Geçerli parametreler girin", color = AppColors.Slate400, style = AppType.body)
                    }
                }
            }

            if (cost != null) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Amber900.copy(alpha = 0.22f)),
                    border = BorderStroke(1.dp, AppColors.Amber700.copy(alpha = 0.5f)),
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PriceCheck, contentDescription = null, tint = AppColors.Amber400)
                            Text(" Maliyet", color = Color.White, style = AppType.sectionHeading)
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                        Text("Birim Maliyet", color = AppColors.Slate400, style = AppType.fieldLabel)
                        Text("${formatTl(cost!!.singleCost)} TL / adet", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                        Text("Toplam Maliyet ($quantityValue adet)", color = AppColors.Amber400, style = AppType.fieldLabel)
                        Text("${formatTl(cost!!.totalCost)} TL", color = Color.White, style = AppType.resultValueMedium)
                        val priceVal = com.dayandisli.calculator.domain.parseLocaleDouble(pricePerKg) ?: 0.0
                        val weightKg = state.result?.weightKg ?: 0.0
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.xs))
                        Text(
                            "(${weightKg.toFixedJs(2)} kg × ${priceVal.toFixedJs(2)} TL/kg${if (quantityValue > 1) " × $quantityValue adet" else ""})",
                            color = AppColors.Slate500,
                            style = AppType.supporting,
                        )
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))
            Text(
                "Formül: ${formulaTextFor(geometry)}",
                color = AppColors.Slate500,
                style = AppType.supporting,
            )
            Text("Ağırlık = Hacim × Yoğunluk", color = AppColors.Slate500, style = AppType.supporting)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))
        }
    }
}

private fun formulaTextFor(geometry: GeometryType): String = when (geometry) {
    GeometryType.SOLID -> "Hacim = π × (çap/2)² × uzunluk"
    GeometryType.PIPE -> "Hacim = (π/4) × (D_dış² - D_iç²) × uzunluk"
    GeometryType.FLAT -> "Hacim = genişlik × yükseklik × uzunluk"
}

/** Matches JS's toLocaleString("tr-TR", { minimumFractionDigits: 2, maximumFractionDigits: 2 }). */
private fun formatTl(value: Double): String {
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("tr-TR"))
    val format = DecimalFormat("#,##0.00", symbols)
    return format.format(value)
}
