package com.dayandisli.calculator.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                GeometryTypeSelector(selected = geometry, onSelected = viewModel::setGeometry)

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

                if (state.validationError != null) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Scale, contentDescription = null, tint = AppColors.Red400, modifier = Modifier.padding(end = 4.dp))
                        Text(state.validationError!!, color = AppColors.Red400, style = AppType.body)
                    }
                }
            }

            if (state.result != null && state.validationError == null) {
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

                        val result = state.result!!
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
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 80.dp))
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

private fun descriptionFor(type: GeometryType): String = when (type) {
    GeometryType.SOLID -> "Dolu silindirik parça"
    GeometryType.PIPE -> "İçi boş silindirik parça"
    GeometryType.FLAT -> "Dikdörtgen kesit"
}

/** Replaces the plain "Geometri Tipi" dropdown with three equally-weighted selectable cards, each
 * showing a native Compose-drawn representative shape, the geometry name, and a short
 * description. Selecting a card calls [onSelected] exactly like the old dropdown did, so the rest
 * of the form's field-switching/validation/calculation behavior is unchanged. Lays out as one row
 * on wide screens and stacks full-width on narrow ones so nothing clips. */
@Composable
private fun GeometryTypeSelector(selected: GeometryType, onSelected: (GeometryType) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(bottom = Spacing.md)) {
        Text("Geometri Tipi", style = AppType.fieldLabel, color = AppColors.Slate300)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 4.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 480.dp
            if (isWide) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                    GeometryType.entries.forEach { type ->
                        GeometryTypeCard(
                            type = type,
                            selected = type == selected,
                            onClick = { onSelected(type) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                    GeometryType.entries.forEach { type ->
                        GeometryTypeCard(
                            type = type,
                            selected = type == selected,
                            onClick = { onSelected(type) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeometryTypeCard(type: GeometryType, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) AppColors.Emerald900.copy(alpha = 0.55f) else AppColors.Slate900.copy(alpha = 0.5f),
        ),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) AppColors.Emerald400 else AppColors.Slate700),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (selected) {
                    com.dayandisli.calculator.ui.components.StatusChip("Seçili", ModuleAccent.Weight)
                }
            }
            GeometryShapeIcon(
                type = type,
                tint = if (selected) AppColors.Emerald400 else AppColors.Slate400,
                modifier = Modifier.size(44.dp),
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.xs))
            Text(
                type.label,
                color = if (selected) Color.White else AppColors.Slate300,
                style = AppType.fieldLabel,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                descriptionFor(type),
                color = if (selected) AppColors.Slate300 else AppColors.Slate500,
                style = AppType.supporting,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Simple, clean representative engineering shapes drawn natively (no emoji/images): a filled
 * disc for a solid cylinder, a ring for a hollow pipe, and a flat bar for a rectangular section. */
@Composable
private fun GeometryShapeIcon(type: GeometryType, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val shortSide = size.minDimension
        when (type) {
            GeometryType.SOLID -> {
                drawCircle(color = tint, radius = shortSide / 2f * 0.85f, center = center)
            }
            GeometryType.PIPE -> {
                val strokeWidth = shortSide * 0.22f
                drawCircle(
                    color = tint,
                    radius = (shortSide / 2f * 0.85f) - strokeWidth / 2f,
                    center = center,
                    style = Stroke(width = strokeWidth),
                )
            }
            GeometryType.FLAT -> {
                val barHeight = size.height * 0.36f
                val barWidth = size.width * 0.82f
                drawRoundRect(
                    color = tint,
                    topLeft = Offset((size.width - barWidth) / 2f, (size.height - barHeight) / 2f),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barHeight * 0.25f, barHeight * 0.25f),
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun GeometryCardPreviewSelected() {
    com.dayandisli.calculator.ui.theme.DayanCalculatorTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GeometryTypeCard(
                type = GeometryType.SOLID,
                selected = true,
                onClick = {},
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun GeometryCardPreviewUnselected() {
    com.dayandisli.calculator.ui.theme.DayanCalculatorTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            GeometryTypeCard(
                type = GeometryType.PIPE,
                selected = false,
                onClick = {},
            )
        }
    }
}
