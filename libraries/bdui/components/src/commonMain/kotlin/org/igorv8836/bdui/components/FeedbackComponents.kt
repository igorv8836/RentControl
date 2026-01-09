package org.igorv8836.bdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.ProgressElement
import org.igorv8836.bdui.contract.ProgressStyle
import org.igorv8836.bdui.contract.SnackbarElement
import org.igorv8836.bdui.contract.StateElement
import org.igorv8836.bdui.contract.StateKind

@Composable
fun SnackbarComponent(
    node: SnackbarElement,
    modifier: Modifier = Modifier,
    onAction: (String?) -> Unit = {},
    messageColor: Color? = null,
    backgroundColor: Color? = null,
    actionTextColor: Color? = null,
) {
    val actionSlot: (@Composable () -> Unit)? = node.actionText?.let { text ->
        { TextButton(onClick = { onAction(node.actionId) }) { Text(text, color = Color.Unspecified) } }
    }
    if (messageColor == null && backgroundColor == null && actionTextColor == null) {
        Snackbar(
            modifier = modifier.fillMaxWidth(),
            action = actionSlot,
        ) { Text(text = node.message) }
        return
    }
    Snackbar(
        modifier = modifier.fillMaxWidth(),
        containerColor = backgroundColor ?: MaterialTheme.colorScheme.inverseSurface,
        contentColor = messageColor ?: MaterialTheme.colorScheme.inverseOnSurface,
        actionContentColor = actionTextColor ?: MaterialTheme.colorScheme.inversePrimary,
        action = actionSlot,
    ) {
        Text(text = node.message, color = Color.Unspecified)
    }
}

@Composable
fun StateComponent(
    node: StateElement,
    modifier: Modifier = Modifier,
    onAction: ((String?) -> Unit)? = null,
    textColor: Color? = null,
    backgroundColor: Color? = null,
    actionTextColor: Color? = null,
) {
    val appliedModifier = if (backgroundColor == null) {
        modifier.fillMaxWidth().padding(12.dp)
    } else {
        modifier.fillMaxWidth().background(backgroundColor).padding(12.dp)
    }
    Column(
        modifier = appliedModifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (node.state) {
            StateKind.Loading -> CircularProgressIndicator()
            StateKind.Empty -> Text("Nothing here", color = textColor ?: Color.Unspecified)
            StateKind.Error -> Text(node.message ?: "Error", color = textColor ?: Color.Unspecified)
            StateKind.Success -> Text(node.message ?: "Success", color = textColor ?: Color.Unspecified)
        }
        node.actionId?.let { actionId ->
            Button(
                onClick = { onAction?.invoke(actionId) },
                colors = if (actionTextColor == null) {
                    ButtonDefaults.buttonColors()
                } else {
                    ButtonDefaults.buttonColors(contentColor = actionTextColor)
                },
            ) { Text("Action", color = Color.Unspecified) }
        }
    }
}

@Composable
fun ProgressComponent(
    node: ProgressElement,
    modifier: Modifier = Modifier,
    indicatorColor: Color? = null,
    trackColor: Color? = null,
) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        when (node.style) {
            ProgressStyle.Linear -> LinearProgressIndicator(
                progress = node.progress ?: 0.3f,
                modifier = Modifier.fillMaxWidth(),
                color = indicatorColor ?: MaterialTheme.colorScheme.primary,
                trackColor = trackColor ?: MaterialTheme.colorScheme.surfaceVariant,
            )
            ProgressStyle.Circular -> CircularProgressIndicator(
                progress = node.progress ?: 0.3f,
                color = indicatorColor ?: MaterialTheme.colorScheme.primary,
                trackColor = trackColor ?: MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
