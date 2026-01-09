package org.igorv8836.bdui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.igorv8836.bdui.contract.ModalElement

@Composable
fun ModalComponent(
    node: ModalElement,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    scrimColor: Color? = null,
    onPrimaryAction: (() -> Unit)? = null,
    onDismissAction: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrimColor ?: Color.Black.copy(alpha = 0.32f))
                .let { base ->
                    if (onDismissAction == null) base else {
                        base.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onDismissAction,
                        )
                    }
                }
        )
        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {},
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor ?: MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                content()
                if (onPrimaryAction != null || onDismissAction != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        if (onDismissAction != null) {
                            Button(onClick = onDismissAction) { Text("Dismiss") }
                        }
                        if (onPrimaryAction != null) {
                            Button(onClick = onPrimaryAction) { Text("OK") }
                        }
                    }
                }
            }
        }
    }
}

