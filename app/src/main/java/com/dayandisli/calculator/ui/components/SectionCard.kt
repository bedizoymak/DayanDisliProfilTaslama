package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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

/** Generic dark slate section card with a heading, matching the `bg-slate-800/50 border-slate-700`
 * cards used throughout the app's forms and result panels. An optional leading icon + accent
 * reinforces which module the section belongs to (Spur/Helical/Weight). */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: ModuleAccent = ModuleAccent.Neutral,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = AppColors.Slate800),
        border = BorderStroke(1.dp, AppColors.Slate700),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = accent.onSoft, modifier = Modifier.padding(end = Spacing.xs))
                }
                Text(title, color = Color.White, style = AppType.sectionHeading)
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            ColumnScopeContent(content)
        }
    }
}

// Small indirection so callers can write a normal Column content lambda without importing
// ColumnScope explicitly at every call site.
typealias ColumnScope = androidx.compose.foundation.layout.ColumnScope

@Composable
private fun ColumnScopeContent(content: @Composable ColumnScope.() -> Unit) {
    Column(content = content)
}
