package org.igorv8836.bdui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
) {
    var value by remember(node.id) { mutableStateOf(node.value) }
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
        )
    }
}

@Composable
fun DropdownComponent(
    node: DropdownElement,
    modifier: Modifier = Modifier,
    onAction: (String?) -> Unit = {},
) {
    var expanded by remember(node.id) { mutableStateOf(false) }
    var selectedIndex by remember(node.id) { mutableStateOf(node.selectedIndex ?: 0) }
    Column(modifier = modifier.fillMaxWidth()) {
        Button(onClick = { expanded = true }) { Text(text = node.label) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            node.options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selectedIndex = index
                        expanded = false
                        onAction(node.actionId)
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = node.options.getOrNull(selectedIndex).orEmpty())
    }
}

@Composable
fun SliderComponent(
    node: SliderElement,
    modifier: Modifier = Modifier,
    onAction: (String?) -> Unit = {},
) {
    var value by remember(node.id) { mutableStateOf(node.value.coerceIn(node.rangeStart, node.rangeEnd)) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = "${node.rangeStart} - ${node.rangeEnd}")
        Slider(
            value = value,
            onValueChange = {
                value = it
            },
            valueRange = node.rangeStart..node.rangeEnd,
            onValueChangeFinished = { onAction(node.actionId) },
        )
        Text(text = value.toString())
    }
}

@Composable
fun SwitchComponent(
    node: SwitchElement,
    modifier: Modifier = Modifier,
    onAction: (String?) -> Unit = {},
) {
    var checked by remember(node.id) { mutableStateOf(node.checked) }
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
        )
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                onAction(node.actionId)
            },
        )
    }
}
