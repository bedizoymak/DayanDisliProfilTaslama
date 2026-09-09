package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.ui.navigation.Destinations
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Dimens
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

private data class NavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val navItems = listOf(
    NavItem(Destinations.HOME, "Ana Sayfa", Icons.Filled.Home),
    NavItem(Destinations.MACHINES, "Makineler", Icons.Filled.Settings),
    NavItem(Destinations.WEIGHT, "Ağırlık Hesaplama", Icons.Filled.Scale),
)

/**
 * Coherent nav shell across form factors: a compact brand top bar plus the three primary
 * destinations (Ana Sayfa / Makineler / Ağırlık Hesaplama). On narrow phones the nav lives in a
 * bottom bar with a subtle selected-state pill; on wide screens (tablets, landscape) it moves to
 * a side rail with the same selected treatment, so the app reads as one system regardless of
 * screen size.
 */
@Composable
fun AppScaffold(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(containerColor = AppColors.Slate900) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = Spacing.md))
                    navItems.forEach { item ->
                        NavigationRailItem(
                            selected = currentRoute == item.route,
                            onClick = { onNavigate(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(Dimens.iconSizeMedium)) },
                            label = { Text(item.label, style = AppType.supporting) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = AppColors.Blue400,
                                indicatorColor = AppColors.Blue600.copy(alpha = 0.28f),
                                unselectedIconColor = AppColors.Slate400,
                                unselectedTextColor = AppColors.Slate500,
                            ),
                        )
                    }
                }
                Scaffold(
                    modifier = Modifier.weight(1f),
                    topBar = { BrandTopBar() },
                    containerColor = AppColors.Slate900,
                ) { padding ->
                    content(Modifier.padding(padding))
                }
            }
        } else {
            Scaffold(
                topBar = { BrandTopBar() },
                bottomBar = {
                    NavigationBar(containerColor = AppColors.Slate900, tonalElevation = 0.dp) {
                        navItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = { onNavigate(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(Dimens.iconSizeMedium)) },
                                label = { Text(item.label, style = AppType.supporting) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = AppColors.Blue400,
                                    indicatorColor = AppColors.Blue600.copy(alpha = 0.28f),
                                    unselectedIconColor = AppColors.Slate400,
                                    unselectedTextColor = AppColors.Slate500,
                                ),
                            )
                        }
                    }
                },
                containerColor = AppColors.Slate900,
            ) { padding ->
                content(Modifier.padding(padding))
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun BrandTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(AppColors.Blue600, shape = RoundedCornerShape(Radius.sm)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(Spacing.sm))
                androidx.compose.foundation.layout.Column {
                    Text("DAYAN CALCULATOR", color = Color.White, style = AppType.cardTitle)
                    Text("Dişli hesaplama aracı", color = AppColors.Slate400, style = AppType.supporting)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Slate900),
        modifier = Modifier.height(Dimens.topBarHeight),
    )
}
