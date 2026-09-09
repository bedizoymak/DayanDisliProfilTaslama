package com.dayandisli.calculator.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dayandisli.calculator.R
import com.dayandisli.calculator.ui.components.ContentContainer
import com.dayandisli.calculator.ui.navigation.Destinations
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Dimens
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

private data class FeatureTile(val label: String, val desc: String)

private val features = listOf(
    FeatureTile("Düz Dişli", "Spur gear"),
    FeatureTile("Helis Dişli", "Helical gear"),
    FeatureTile("Taksimat", "Division calc"),
    FeatureTile("PDF Export", "Reçete çıktısı"),
)

/** Reproduces pages/CalculatorDashboard.tsx: centered hero, two primary module cards
 * (Makineler / Ağırlık Hesaplama), and a 2x2 feature grid. */
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        ContentContainer(
            maxWidth = Dimens.formMaxWidth,
            modifier = Modifier.padding(horizontal = Dimens.screenPaddingHorizontal, vertical = Dimens.screenPaddingVertical),
        ) {
            // Hero
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.dayan_logo_header),
                    contentDescription = "DAYAN",
                    modifier = Modifier
                        .width(250.dp)
                        .height(78.dp),
                    contentScale = ContentScale.Fit,
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.lg))
                Text(
                    "DAYAN Dişli Hesap Makinesi",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
                Text(
                    "Dişli üretimi için hesaplama ve ayar aracı",
                    color = AppColors.Slate400,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
                Text(
                    "Önce makinenizi seçin, sonra dişli tipini ve parametreleri girerek üretim reçetesi (PDF) oluşturun.",
                    color = AppColors.Slate500,
                    style = AppType.body,
                    textAlign = TextAlign.Center,
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.xxl))

            // Main cards
            Card(
                onClick = { onNavigate(Destinations.MACHINES) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(containerColor = AppColors.Slate800.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, AppColors.Slate700),
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Image(
                        painter = painterResource(R.drawable.dayan_machine_icon_v1),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit,
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                    Text("Makineler", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        "Makine seçimi yaparak dişli hesaplamalarına başlayın. Düz ve helis dişli hesaplama modülleri mevcuttur.",
                        color = AppColors.Slate400,
                        style = AppType.body,
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                    Button(
                        onClick = { onNavigate(Destinations.MACHINES) },
                        modifier = Modifier.fillMaxWidth().height(Dimens.fieldHeight),
                        shape = RoundedCornerShape(Radius.md),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue600),
                    ) {
                        Text("Makine Seçimi")
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = Spacing.xs))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))

            Card(
                onClick = { onNavigate(Destinations.WEIGHT) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.lg),
                colors = CardDefaults.cardColors(containerColor = AppColors.Slate800.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, AppColors.Slate700),
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                    Image(
                        painter = painterResource(R.drawable.dayan_weight_icon_v1),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit,
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                    Text("Ağırlık Hesaplama", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 4.dp))
                    Text(
                        "Ham malzeme ağırlığını hesaplayın. Silindirik parçalar için tahmini ağırlık hesaplama aracı.",
                        color = AppColors.Slate400,
                        style = AppType.body,
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                    OutlinedButton(
                        onClick = { onNavigate(Destinations.WEIGHT) },
                        modifier = Modifier.fillMaxWidth().height(Dimens.fieldHeight),
                        shape = RoundedCornerShape(Radius.md),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Emerald400),
                        border = BorderStroke(1.dp, AppColors.Emerald600.copy(alpha = 0.5f)),
                    ) {
                        Text("Hesaplamaya Git")
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = Spacing.xs))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.xxl))

            // Feature grid: 2 columns x 2 rows
            features.chunked(2).forEach { rowFeatures ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    rowFeatures.forEach { feature ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(Radius.md),
                            colors = CardDefaults.cardColors(containerColor = AppColors.Slate800.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, AppColors.Slate700.copy(alpha = 0.5f)),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(feature.label, color = Color.White, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                                Text(feature.desc, color = AppColors.Slate500, style = AppType.supporting, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.sm))
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 80.dp))
        }
    }
}
