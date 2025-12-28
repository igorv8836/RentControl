package org.igorv8836.bdui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
) {
    Snackbar(
        modifier = modifier.fillMaxWidth(),
        action = node.actionText?.let { text ->
            {
                Button(onClick = { onAction(node.actionId) }) {
                    Text(text)
                }
            }
        }
    ) { Text(text = node.message) }
}

@Composable
fun StateComponent(
    node: StateElement,
    modifier: Modifier = Modifier,
    onAction: ((String?) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (node.state) {
            StateKind.Loading -> CircularProgressIndicator()
            StateKind.Empty -> Text("Nothing here")
            StateKind.Error -> Text(node.message ?: "Error")
            StateKind.Success -> Text(node.message ?: "Success")
        }
        node.actionId?.let { actionId ->
            Button(onClick = { onAction?.invoke(actionId) }) { Text("Action") }
        }
    }
}

@Composable
fun ProgressComponent(
    node: ProgressElement,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        when (node.style) {
            ProgressStyle.Linear -> LinearProgressIndicator(
                progress = node.progress ?: 0.3f,
                modifier = Modifier.fillMaxWidth()
            )
            ProgressStyle.Circular -> CircularProgressIndicator(progress = node.progress ?: 0.3f)
        }
    }
}
