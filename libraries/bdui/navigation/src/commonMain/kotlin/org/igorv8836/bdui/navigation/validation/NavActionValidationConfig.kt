package org.igorv8836.bdui.navigation.validation

data class NavActionValidationConfig(
    val allowedSchemes: Set<String> = setOf("http", "https"),
    val maxPathLength: Int = 2048,
)