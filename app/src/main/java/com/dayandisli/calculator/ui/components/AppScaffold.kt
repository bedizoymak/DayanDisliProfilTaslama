package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.ui.navigation.Destinations
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

private data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val navItems = listOf(
    NavItem(Destinations.HOME, "Ana Sayfa", Icons.Filled.Home),
    NavItem(Destinations.MACHINES, "Makineler", Icons.Filled.Settings),
    NavItem(Destinations.WEIGHT, "Ağırlık Hesaplama", Icons.Filled.Scale),
)

private fun isPrimaryDestinationSelected(currentRoute: String?, itemRoute: String): Boolean = when (itemRoute) {
    Destinations.HOME -> currentRoute == Destinations.HOME
    Destinations.MACHINES -> currentRoute == Destinations.MACHINES ||
        currentRoute?.startsWith("machines/") == true ||
        currentRoute?.startsWith("spurFlow/") == true ||
        currentRoute?.startsWith("helicalFlow/") == true ||
        currentRoute?.startsWith("reishauerFlow/") == true
    else -> currentRoute == itemRoute
}

/**
 * Coherent nav shell across form factors: the three primary destinations (Ana Sayfa / Makineler /
 * Ağırlık Hesaplama) as one fixed top navigation bar - no separate brand/logo header. Always
 * rendered the same way on phones, tablets, and landscape alike, so the app reads as one single
 * navigation system rather than switching layouts by width. There is exactly one shell wrapping
 * the entire NavHost; individual screens never render their own navigation bar.
 */
@Composable
fun AppScaffold(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        topBar = { PrimaryTopBar(currentRoute = currentRoute, onNavigate = onNavigate) },
        containerColor = AppColors.Slate900,
    ) { padding ->
        content(Modifier.padding(padding))
    }
}

/** Fixed top navigation: exactly three equal-width segments (each `Modifier.weight(1f)`) in one
 * row, so destinations sit close together with no floating/oddly-spaced controls regardless of
 * screen width. Selected segment gets a clearly visible rounded blue background; inactive
 * segments stay dark navy/slate. Icons/labels are sized up (26dp / 13sp) and each segment has a
 * tall (~72dp) touch target for comfortable shop-floor tapping. Replaces the old separate
 * brand/logo header bar entirely - this is the only top bar. */
@Composable
private fun PrimaryTopBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Slate900)
            .statusBarsPadding()
            .height(72.dp)
            .padding(horizontal = Spacing.xs, vertical = Spacing.xxs),
    ) {
        navItems.forEach { item ->
            val selected = isPrimaryDestinationSelected(currentRoute, item.route)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = Spacing.xxs)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(if (selected) AppColors.Blue600.copy(alpha = 0.9f) else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onNavigate(item.route) },
                    )
                    .semantics {
                        role = Role.Tab
                        this.selected = selected
                        contentDescription = item.label
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else AppColors.Slate400,
                    modifier = Modifier.size(26.dp),
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(2.dp))
                Text(
                    item.label,
                    style = AppType.fieldLabel,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) Color.White else AppColors.Slate400,
                    maxLines = 1,
                )
            }
        }
    }
}
