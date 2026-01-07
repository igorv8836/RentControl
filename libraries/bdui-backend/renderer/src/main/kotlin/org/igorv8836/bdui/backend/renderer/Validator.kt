package org.igorv8836.bdui.backend.renderer

import org.igorv8836.bdui.backend.core.LimitConfig
import org.igorv8836.bdui.backend.core.ValidationIssue
import org.igorv8836.bdui.contract.ComponentNode
import org.igorv8836.bdui.contract.Container
import org.igorv8836.bdui.contract.DividerElement
import org.igorv8836.bdui.contract.LazyListElement
import org.igorv8836.bdui.contract.ListItemElement
import org.igorv8836.bdui.contract.Scaffold
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.contract.TextElement

fun validateScreen(remoteScreen: RemoteScreen, limits: LimitConfig): List<ValidationIssue> {
    val issues = mutableListOf<ValidationIssue>()

    if (remoteScreen.id.isBlank()) {
        issues += ValidationIssue(path = "screen.id", message = "Screen id must not be blank", code = "blank_id")
    }
    if (remoteScreen.version < 0) {
        issues += ValidationIssue(path = "screen.version", message = "Screen version must be non-negative", code = "invalid_version")
    }
    if (remoteScreen.actions.size > limits.maxActions) {
        issues += ValidationIssue(
            path = "screen.actions",
            message = "Too many actions: ${remoteScreen.actions.size} > ${limits.maxActions}",
            code = "actions_limit",
        )
    }
    if (remoteScreen.triggers.size > limits.maxTriggers) {
        issues += ValidationIssue(
            path = "screen.triggers",
            message = "Too many triggers: ${remoteScreen.triggers.size} > ${limits.maxTriggers}",
            code = "triggers_limit",
        )
    }

    val seenIds = mutableSetOf<String>()
    val totalNodes = traverseScreen(remoteScreen.layout) { node, depth ->
        if (!seenIds.add(node.id)) {
            issues += ValidationIssue(
                path = "components.${node.id}",
                message = "Duplicate component id '${node.id}'",
                code = "duplicate_id",
            )
        }
        if (depth > limits.maxDepth) {
            issues += ValidationIssue(
                path = "components.${node.id}",
                message = "Depth $depth exceeds maxDepth ${limits.maxDepth}",
                code = "depth_limit",
            )
        }
        when (node) {
            is Container -> {
                if (node.children.size > limits.maxChildrenPerNode) {
                    issues += ValidationIssue(
                        path = "components.${node.id}.children",
                        message = "Too many children: ${node.children.size} > ${limits.maxChildrenPerNode}",
                        code = "children_limit",
                    )
                }
            }
            is TextElement -> {
                node.template?.let { tpl ->
                    if (tpl.length > limits.maxTemplateLength) {
                        issues += ValidationIssue(
                            path = "components.${node.id}.template",
                            message = "Template too long (${tpl.length}) > ${limits.maxTemplateLength}",
                            code = "template_limit",
                        )
                    }
                }
            }
            is ListItemElement -> Unit
            is DividerElement -> Unit
            is LazyListElement -> Unit
            else -> Unit
        }
    }

    if (totalNodes > limits.maxNodes) {
        issues += ValidationIssue(
            path = "components",
            message = "Too many nodes: $totalNodes > ${limits.maxNodes}",
            code = "nodes_limit",
        )
    }

    return issues
}

private fun traverseScreen(
    layout: org.igorv8836.bdui.contract.Layout,
    visit: (ComponentNode, depth: Int) -> Unit,
): Int {
    var count = 0
    fun walk(node: ComponentNode, depth: Int) {
        count++
        visit(node, depth)
        childrenOf(node).forEach { walk(it, depth + 1) }
    }
    layout.scaffold?.top?.let { walk(it, depth = 1) }
    layout.sections.forEach { section ->
        walk(section.content, depth = 1)
    }
    layout.root?.let { walk(it, depth = 1) }
    layout.scaffold?.bottom?.let { walk(it, depth = 1) }
    return count
}

private fun childrenOf(node: ComponentNode): List<ComponentNode> =
    when (node) {
        is Container -> node.children
        is LazyListElement -> node.items
        else -> emptyList()
    }
