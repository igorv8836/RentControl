package org.igorv8836.bdui.renderer.node

import androidx.compose.ui.graphics.Color
import org.igorv8836.bdui.contract.Color as ContractColor

internal fun parseColor(color: ContractColor?, isDark: Boolean): Color? {
    val hex = when {
        color == null -> return null
        isDark && !color.dark.isNullOrBlank() -> color.dark
        else -> color.light
    } ?: return null
    val clean = hex.removePrefix("#")
    val argbInt = when (clean.length) {
        6 -> {
            val rgb = clean.toLong(16).toInt()
            (0xFF shl 24) or rgb
        }
        8 -> clean.toLong(16).toInt()
        else -> return null
    }
    return runCatching { Color(argbInt) }.getOrNull()
}
