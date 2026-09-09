package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.ModuleAccent
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing

/**
 * Centers content and caps its width on large/tablet screens so layouts don't stretch
 * edge-to-edge, while using the full available width (minus horizontal padding) on phones.
 */
@Composable
fun ContentContainer(
    maxWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = maxWidth).fillMaxWidth(), content = content)
    }
}

/** Screen-level header: eyebrow/context line (optional), bold title, quiet subtitle. */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    eyebrow: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (eyebrow != null) {
            Text(eyebrow, style = AppType.fieldLabel, color = AppColors.Slate400)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 2.dp))
        }
        Text(title, style = AppType.screenTitle, color = Color.White)
        if (subtitle != null) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(subtitle, style = AppType.screenSubtitle, color = AppColors.Slate400)
        }
    }
}

/** Small rounded icon badge used for module identity across Home / Machines / Detail cards. */
@Composable
fun ModuleIconBadge(
    icon: ImageVector,
    accent: ModuleAccent,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(accent.soft, RoundedCornerShape(Radius.md)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = accent.onSoft, modifier = Modifier.size(size * 0.5f))
    }
}

/** Small status/label chip, e.g. "PERFECT MATCH", module tags, count badges. */
@Composable
fun StatusChip(text: String, accent: ModuleAccent, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(accent.soft, RoundedCornerShape(Radius.pill))
            .padding(horizontal = Spacing.sm, vertical = 4.dp),
    ) {
        Text(text, style = AppType.chip, color = accent.onSoft)
    }
}

/** Thin colored top indicator bar for a card, used to reinforce module identity at a glance. */
@Composable
fun AccentTopBar(accent: ModuleAccent, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .background(accent.strong),
    )
}
