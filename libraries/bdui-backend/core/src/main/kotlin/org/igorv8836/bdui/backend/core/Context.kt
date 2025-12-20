package org.igorv8836.bdui.backend.core

/**
 * Execution context passed through mapper/renderer stages.
 */
data class ExecutionContext(
    val requestId: String? = null,
    val locale: String? = null,
    val featureFlags: Map<String, Boolean> = emptyMap(),
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * Configurable safety limits to prevent oversized screens/payloads.
 */
data class LimitConfig(
    val maxDepth: Int = 50,
    val maxChildrenPerNode: Int = 200,
    val maxActions: Int = 200,
    val maxTemplateLength: Int = 2000,
    val maxNodes: Int = 2000,
    val maxTriggers: Int = 100,
)
