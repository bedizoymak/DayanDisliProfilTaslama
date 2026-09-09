package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.ui.theme.AppColors
import com.dayandisli.calculator.ui.theme.AppType
import com.dayandisli.calculator.ui.theme.Spacing

/** Shared outlined text-field color scheme for the industrial dark theme. Consolidated from the
 * previously duplicated `fieldColors()` helpers in the form screens. */
@Composable
fun engineeringFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = AppColors.Slate900,
    unfocusedContainerColor = AppColors.Slate900,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = AppColors.Blue500,
    unfocusedBorderColor = AppColors.Slate600,
    focusedLabelColor = AppColors.Blue400,
    unfocusedLabelColor = AppColors.Slate400,
    errorBorderColor = AppColors.Red400,
    errorLabelColor = AppColors.Red400,
    cursorColor = AppColors.Blue400,
)

/** Labeled numeric/text field with a unit suffix, consistent height, and a quiet field label
 * above the input (rather than relying on Material's floating label alone). */
@Composable
fun EngineeringTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    unit: String? = null,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    isError: Boolean = false,
) {
    Column(modifier = modifier.padding(bottom = Spacing.md)) {
        Row {
            Text(label, style = AppType.fieldLabel, color = AppColors.Slate300)
            if (supportingText != null) {
                Text(" $supportingText", style = AppType.supporting, color = AppColors.Blue400)
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = engineeringFieldColors(),
            singleLine = true,
            isError = isError,
            suffix = if (unit != null) {
                { Text(unit, style = AppType.supporting, color = AppColors.Slate400) }
            } else {
                null
            },
        )
    }
}

/** Dropdown styled to match [EngineeringTextField] instead of the default ExposedDropdownMenu
 * look, used for enum-backed selection fields (helix direction, geometry type, material). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> EngineeringDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.padding(bottom = Spacing.md)) {
        Text(label, style = AppType.fieldLabel, color = AppColors.Slate300)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.padding(top = 4.dp),
        ) {
            OutlinedTextField(
                value = optionLabel(selected),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                colors = engineeringFieldColors(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
