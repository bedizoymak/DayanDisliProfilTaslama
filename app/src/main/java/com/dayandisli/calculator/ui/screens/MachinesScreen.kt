package com.dayandisli.calculator.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayandisli.calculator.R
import com.dayandisli.calculator.domain.MACHINES
import com.dayandisli.calculator.domain.Machine
import com.dayandisli.calculator.ui.components.ContentContainer
import com.dayandisli.calculator.ui.components.ScreenHeader
import com.dayandisli.calculator.ui.navigation.Destinations
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Dimens
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

/** Real machine photography, ported from CEHA's res/drawable (ic_1..ic_10.jpg). Two machines
 * (Gleason/Hurth/Liebherr beyond what the source photo set covers) fall back to the icon badge. */
private fun machinePhotoRes(machineId: String): Int? = when (machineId) {
    "kucuk-azdirma" -> R.drawable.machine_kucuk_azdirma
    "reishauer-taslama" -> R.drawable.machine_reishauer_taslama
    "yatay-azdirma" -> R.drawable.machine_yatay_azdirma
    "fellows-lorenz-snj5" -> R.drawable.machine_fellows_lorenz_snj5
    "pfauter-p403" -> R.drawable.machine_pfauter_p403
    "fellows-lorenz-s71" -> R.drawable.machine_fellows_lorenz_s71
    "azdirma-genel" -> R.drawable.machine_azdirma_genel
    "torna-tezgahi" -> R.drawable.machine_torna_tezgahi
    "silindirik-taslama" -> R.drawable.machine_silindirik_taslama
    "gleason" -> R.drawable.machine_gleason
    else -> null
}

private val supportedMachineIds = setOf("kucuk-azdirma", "reishauer-taslama")

/** Reproduces pages/MachineSelection.tsx: responsive grid of machine cards, each showing the real
 * machine photo (with a dark gradient scrim for text legibility) or an icon badge fallback, name,
 * description, and a ghost "Seç" button with a trailing chevron. */
@Composable
fun MachinesScreen(onMachineSelected: (String) -> Unit) {
    ContentContainer(
        maxWidth = Dimens.receiptMaxWidth,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical)) {
            ScreenHeader(
                title = "Makine Seçimi",
                subtitle = "Hesaplama yapmak istediğiniz makineyi seçin",
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 280.dp),
            contentPadding = PaddingValues(start = Dimens.screenPaddingHorizontal, top = Spacing.xs, end = Dimens.screenPaddingHorizontal, bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(MACHINES, key = { it.id }) { machine ->
                MachineCard(
                    machine = machine,
                    enabled = machine.id in supportedMachineIds,
                    onClick = { onMachineSelected(machine.id) },
                )
            }
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))
    }
}

@Composable
private fun MachineCard(machine: Machine, enabled: Boolean, onClick: () -> Unit) {
    val photoRes = machinePhotoRes(machine.id)
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = AppColors.Slate800.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, if (enabled) AppColors.Slate700 else AppColors.Slate800),
    ) {
        Column {
            if (photoRes != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg)),
                ) {
                    Image(
                        painter = painterResource(photoRes),
                        contentDescription = "${machine.name} fotoğrafı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, AppColors.Slate900.copy(alpha = 0.85f)),
                                    startY = 0.35f,
                                ),
                            ),
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(AppColors.Slate900, RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = AppColors.Slate500, modifier = Modifier.height(40.dp))
                }
            }

            Column(modifier = Modifier.padding(Spacing.lg)) {
                Text(machine.name, color = if (enabled) Color.White else AppColors.Slate400, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 2.dp))
                Text(machine.description, color = AppColors.Slate500, style = AppType.body)
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                TextButton(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth().height(Dimens.minTouchTarget),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppColors.Slate300,
                        disabledContentColor = AppColors.Slate600,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(if (enabled) "Seç" else "Henüz Hazır Değil")
                        if (enabled) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, modifier = Modifier.height(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun MachineCardPreviewEnabled() {
    com.dayandisli.calculator.ui.theme.DayanCalculatorTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MachineCard(
                machine = MACHINES[0],
                enabled = true,
                onClick = {},
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun MachineCardPreviewDisabled() {
    com.dayandisli.calculator.ui.theme.DayanCalculatorTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MachineCard(
                machine = MACHINES[2],
                enabled = false,
                onClick = {},
            )
        }
    }
}
