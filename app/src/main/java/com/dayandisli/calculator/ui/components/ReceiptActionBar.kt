package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.ui.theme.Dimens
import com.dayandisli.calculator.ui.theme.ModuleAccent
import com.dayandisli.calculator.ui.theme.Radius
import com.dayandisli.calculator.ui.theme.Spacing
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

/** Coherent Paylaş/Kaydet/Yazdır action toolbar for receipt screens. Wraps to a new line on
 * narrow phones instead of clipping/overflowing, and stays a single row on wide screens. Does not
 * touch the underlying PdfReceiptGenerator calls — callers still own onShare/onSave/onPrint. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReceiptActionBar(
    accent: ModuleAccent,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onPrint: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Button(
            onClick = onShare,
            modifier = Modifier.height(Dimens.minTouchTarget).wrapContentWidth(),
            shape = RoundedCornerShape(Radius.md),
            colors = ButtonDefaults.buttonColors(containerColor = accent.strong),
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(" Paylaş")
        }
        OutlinedButton(
            onClick = onSave,
            modifier = Modifier.height(Dimens.minTouchTarget).wrapContentWidth(),
            shape = RoundedCornerShape(Radius.md),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = accent.onSoft),
        ) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(" Kaydet")
        }
        OutlinedButton(
            onClick = onPrint,
            modifier = Modifier.height(Dimens.minTouchTarget).wrapContentWidth(),
            shape = RoundedCornerShape(Radius.md),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = accent.onSoft),
        ) {
            Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(" Yazdır")
        }
    }
}
