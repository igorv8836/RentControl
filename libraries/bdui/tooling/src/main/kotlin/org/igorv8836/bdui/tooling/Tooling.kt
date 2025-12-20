package org.igorv8836.bdui.tooling

import org.igorv8836.bdui.contract.ButtonElement
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.Screen
import org.igorv8836.bdui.contract.TextElement

class SchemaPrinter {
    fun describe(screen: Screen): String {
        val builder = StringBuilder()
        builder.appendLine("screen=${screen.id}@v${screen.version}")
        builder.append(render(node = screen.layout.root, indent = 0))
        return builder.toString()
    }

    private fun render(node: ComponentNode, indent: Int): String {
        val prefix = "  ".repeat(indent)
        val line = when (node) {
            is TextElement -> "$prefix- text(${node.id}) key=${node.textKey}"
            is ButtonElement -> "$prefix- button(${node.id}) action=${node.actionId}"
            is Container -> {
                val containerLine = "$prefix- container(${node.id}) dir=${node.direction}"
                val children = node.children.joinToString(separator = "\n") { child ->
                    render(child, indent + 1)
                }
                if (children.isEmpty()) containerLine else containerLine + "\n" + children
            }

            else -> "$prefix- node(${node.id})"
        }
        return line
    }
}

fun main() {
    println("BDUI tooling is ready. Use SchemaPrinter to inspect payloads.")
}
