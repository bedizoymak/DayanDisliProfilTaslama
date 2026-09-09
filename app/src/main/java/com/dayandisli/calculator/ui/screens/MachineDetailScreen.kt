package com.dayandisli.calculator.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.R
import com.dayandisli.calculator.domain.getMachineById
import com.dayandisli.calculator.ui.components.ContentContainer
import com.dayandisli.calculator.ui.components.ModuleIconBadge
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Dimens
import com.dayandisli.calculator.ui.theme.ModuleAccent
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

/** Machine name dominant, followed by action cards for the two shipped calculation modules
 * (Düz Dişli / Helis Dişli). No placeholder/unsupported operations are surfaced here. */
@Composable
fun MachineDetailScreen(machineId: String, onSpur: () -> Unit, onHelical: () -> Unit) {
    val machine = getMachineById(machineId)

    ContentContainer(
        maxWidth = Dimens.formMaxWidth,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical)
            .fillMaxWidth(),
    ) {
        if (machine == null) {
            Text("Makine bulunamadı.", color = AppColors.Slate400)
            return@ContentContainer
        }

        Card(
            shape = RoundedCornerShape(Radius.lg),
            colors = CardDefaults.cardColors(containerColor = AppColors.Slate800.copy(alpha = 0.7f)),
            border = BorderStroke(1.dp, AppColors.Slate700),
        ) {
            Row(modifier = Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                ModuleIconBadge(icon = Icons.Filled.Settings, accent = ModuleAccent.Spur, size = 56.dp)
                Column(modifier = Modifier.padding(start = Spacing.md)) {
                    Text(machine.name, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text(machine.description, color = AppColors.Slate400, style = AppType.body)
                }
            }
        }

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.xl))
        Text("İşlem Seçimi", color = Color.White, style = AppType.sectionHeading)
        Text(
            "Hesaplamak istediğiniz dişli tipini seçin",
            color = AppColors.Slate400,
            style = AppType.body,
            modifier = Modifier.padding(top = 4.dp, bottom = Spacing.md),
        )

        OperationCard(
            title = "Düz Dişli Hesaplama",
            description = "Spur gear - Düz dişli parametreleri ve Wk değerleri hesaplama",
            icon = Icons.Filled.Settings,
            imageRes = R.drawable.ic_spur_gear,
            accent = ModuleAccent.Spur,
            primary = true,
            onClick = onSpur,
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
        OperationCard(
            title = "Helis Dişli Hesaplama",
            description = "Helical gear - Helisel dişli parametreleri ve kombinasyon hesaplama",
            icon = Icons.Filled.Sync,
            imageRes = R.drawable.ic_helical_gear,
            accent = ModuleAccent.Helical,
            primary = false,
            onClick = onHelical,
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.xl))
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.sm),
        ) {
            listOf("Profil Taşlama" to "Yakında", "İç Dişli" to "Yakında").forEach { (title, desc) ->
                Card(
                    modifier = Modifier.weight(1f).alpha(0.5f),
                    shape = RoundedCornerShape(Radius.md),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Slate800.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, AppColors.Slate700.copy(alpha = 0.5f)),
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(title, color = AppColors.Slate400, style = MaterialTheme.typography.titleSmall)
                        Text(desc, color = AppColors.Slate500, style = AppType.supporting)
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: ModuleAccent,
    primary: Boolean,
    onClick: () -> Unit,
    imageRes: Int? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = AppColors.Slate800),
        border = BorderStroke(1.dp, AppColors.Slate700),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            if (imageRes != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(Radius.md))
                        .background(accent.soft),
                ) {
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.padding(6.dp),
                    )
                }
            } else {
                ModuleIconBadge(icon = icon, accent = accent)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(
                description,
                color = AppColors.Slate400,
                style = AppType.body,
                modifier = Modifier.padding(top = 4.dp, bottom = Spacing.md),
            )
            if (primary) {
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(Dimens.fieldHeight),
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.buttonColors(containerColor = accent.strong),
                ) {
                    Text("Hesaplamaya Başla")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = Spacing.xs))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(Dimens.fieldHeight),
                    shape = RoundedCornerShape(Radius.md),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent.onSoft),
                    border = BorderStroke(1.dp, accent.strong.copy(alpha = 0.6f)),
                ) {
                    Text("Hesaplamaya Başla")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = Spacing.xs))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}
