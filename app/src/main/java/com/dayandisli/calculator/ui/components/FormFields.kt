package com.dayandisli.calculator.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dayandisli.calculator.domain.normalizeDecimalInput
import com.dayandisli.calculator.domain.normalizeIntegerInput
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
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Decimal,
    normalizeInput: Boolean = true,
    formatOnFocusLost: ((String) -> String)? = null,
    transformWhileTyping: ((String) -> String)? = null,
    clearValueOnFirstFocus: String? = null,
    isError: Boolean = false,
) {
    var wasFocused by remember { mutableStateOf(false) }
    var hasReceivedFocus by remember { mutableStateOf(false) }
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(value) {
        if (textFieldValue.text != value) {
            textFieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }
    Column(modifier = modifier.padding(bottom = Spacing.md)) {
        Row {
            Text(label, style = AppType.fieldLabel, color = AppColors.Slate300)
            if (supportingText != null) {
                Text(" $supportingText", style = AppType.supporting, color = AppColors.Blue400)
            }
        }
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { input ->
                val normalized = when {
                        transformWhileTyping != null -> transformWhileTyping(input.text)
                        !normalizeInput -> input.text
                        keyboardType == KeyboardType.Number -> normalizeIntegerInput(input.text)
                        else -> normalizeDecimalInput(input.text)
                    }
                textFieldValue = TextFieldValue(
                    text = normalized,
                    selection = if (normalized != input.text) TextRange(normalized.length) else input.selection,
                )
                onValueChange(normalized)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .onFocusChanged { state ->
                    if (!wasFocused && state.isFocused && !hasReceivedFocus && value == clearValueOnFirstFocus) {
                        onValueChange("")
                    }
                    if (wasFocused && !state.isFocused && formatOnFocusLost != null) {
                        onValueChange(formatOnFocusLost(value))
                    }
                    if (state.isFocused) hasReceivedFocus = true
                    wasFocused = state.isFocused
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = if (formatOnFocusLost != null) ImeAction.Done else ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (formatOnFocusLost != null) onValueChange(formatOnFocusLost(value))
                    focusManager.clearFocus()
                },
            ),
            colors = engineeringFieldColors(),
            singleLine = true,
            isError = isError,
            placeholder = placeholder?.let { { Text(it, style = AppType.body, color = AppColors.Slate500) } },
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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0F172A)
@Composable
fun ReishauerAngleInputPreview() {
    com.dayandisli.calculator.ui.theme.DayanCalculatorTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            EngineeringTextField(
                label = "Kavrama Açısı (α)",
                value = "20°35′06″95",
                onValueChange = {},
                unit = "D,M,S,CS",
            )
        }
    }
}
