package org.igorv8836.bdui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.DropdownElement
import org.igorv8836.bdui.contract.SliderElement
import org.igorv8836.bdui.contract.SwitchElement
import org.igorv8836.bdui.contract.TextFieldElement

@Composable
fun TextFieldComponent(
    node: TextFieldElement,
    modifier: Modifier = Modifier,
    onAction: (String?) -> Unit = {},
    textColor: Color? = null,
    labelColor: Color? = null,
    placeholderColor: Color? = null,
    backgroundColor: Color? = null,
) {
    var value by remember(node.id) { mutableStateOf(node.value) }
    val colors = if (textColor == null && labelColor == null && placeholderColor == null && backgroundColor == null) {
        OutlinedTextFieldDefaults.colors()
    } else {
        val resolvedTextColor = textColor ?: MaterialTheme.colorScheme.onSurface
        val resolvedLabelColor = labelColor ?: MaterialTheme.colorScheme.onSurfaceVariant
        val resolvedPlaceholderColor = placeholderColor ?: MaterialTheme.colorScheme.onSurfaceVariant
        val resolvedContainerColor = backgroundColor ?: Color.Transparent
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = resolvedTextColor,
            unfocusedTextColor = resolvedTextColor,
            focusedLabelColor = resolvedLabelColor,
            unfocusedLabelColor = resolvedLabelColor,
            focusedPlaceholderColor = resolvedPlaceholderColor,
            unfocusedPlaceholderColor = resolvedPlaceholderColor,
            focusedContainerColor = resolvedContainerColor,
            unfocusedContainerColor = resolvedContainerColor,
            disabledContainerColor = resolvedContainerColor,
        )
    }
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
                onAction(node.actionId)
            },
            label = { Text(node.label) },
            placeholder = node.placeholder?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            colors = colors,
        )
    }
}

@Composable
fun DropdownComponent(
    node: DropdownElement,
    modifier: Modifier = Modifier,
    onAction: (String?) -> Unit = {},
    labelColor: Color? = null,
    selectedTextColor: Color? = null,
    backgroundColor: Color? = null,
) {
    var expanded by remember(node.id) { mutableStateOf(false) }
    var selectedIndex by remember(node.id) { mutableStateOf(node.selectedIndex ?: 0) }
    val selectedText = node.options.getOrNull(selectedIndex).orEmpty()
    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { expanded = true },
            colors = if (labelColor == null && backgroundColor == null) {
                ButtonDefaults.buttonColors()
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = backgroundColor ?: MaterialTheme.colorScheme.primary,
                    contentColor = labelColor ?: MaterialTheme.colorScheme.onPrimary,
                )
            },
        ) {
            Text(text = node.label, color = Color.Unspecified)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            node.options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, color = selectedTextColor ?: Color.Unspecified) },
                    onClick = {
                        selectedIndex = index
                        expanded = false
                        onAction(node.actionId)
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = selectedText, color = selectedTextColor ?: Color.Unspecified)
    }
}

@Composable
fun SliderComponent(
    node: SliderElement,
    modifier: Modifier = Modifier,
    onAction: (String?) -> Unit = {},
    textColor: Color? = null,
    thumbColor: Color? = null,
    activeTrackColor: Color? = null,
    inactiveTrackColor: Color? = null,
) {
    var value by remember(node.id) { mutableStateOf(node.value.coerceIn(node.rangeStart, node.rangeEnd)) }
    val colors = if (thumbColor == null && activeTrackColor == null && inactiveTrackColor == null) {
        SliderDefaults.colors()
    } else {
        SliderDefaults.colors(
            thumbColor = thumbColor ?: MaterialTheme.colorScheme.primary,
            activeTrackColor = activeTrackColor ?: MaterialTheme.colorScheme.primary,
            inactiveTrackColor = inactiveTrackColor ?: MaterialTheme.colorScheme.surfaceVariant,
        )
    }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "${node.rangeStart} - ${node.rangeEnd}", color = textColor ?: Color.Unspecified)
        Slider(
            value = value,
            onValueChange = {
                value = it
            },
            valueRange = node.rangeStart..node.rangeEnd,
            onValueChangeFinished = { onAction(node.actionId) },
            colors = colors,
        )
        Text(text = value.toString(), color = textColor ?: Color.Unspecified)
    }
}

@Composable
fun SwitchComponent(
    node: SwitchElement,
    modifier: Modifier = Modifier,
    onAction: (String?) -> Unit = {},
    titleColor: Color? = null,
    checkedThumbColor: Color? = null,
    uncheckedThumbColor: Color? = null,
    checkedTrackColor: Color? = null,
    uncheckedTrackColor: Color? = null,
) {
    var checked by remember(node.id) { mutableStateOf(node.checked) }
    val colors = if (
        checkedThumbColor == null &&
            uncheckedThumbColor == null &&
            checkedTrackColor == null &&
            uncheckedTrackColor == null
    ) {
        SwitchDefaults.colors()
    } else {
        SwitchDefaults.colors(
            checkedThumbColor = checkedThumbColor ?: MaterialTheme.colorScheme.onPrimary,
            uncheckedThumbColor = uncheckedThumbColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            checkedTrackColor = checkedTrackColor ?: MaterialTheme.colorScheme.primary,
            uncheckedTrackColor = uncheckedTrackColor ?: MaterialTheme.colorScheme.surfaceVariant,
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = node.title,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .clickable(role = Role.Switch) {
                    checked = !checked
                    onAction(node.actionId)
                },
            color = titleColor ?: Color.Unspecified,
        )
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onAction(node.actionId)
            },
            colors = colors,
        )
    }
}
