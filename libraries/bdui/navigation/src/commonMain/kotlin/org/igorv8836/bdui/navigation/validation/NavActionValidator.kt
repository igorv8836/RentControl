package org.igorv8836.bdui.navigation.validation

class NavActionValidator(
    private val config: NavActionValidationConfig = NavActionValidationConfig(),
) {
    fun validateForwardPath(path: String?): Result<Unit> {
        if (path == null) return Result.success(Unit)
        if (path.length > config.maxPathLength) {
            return Result.failure(IllegalArgumentException("Path too long"))
        }
        val scheme = path.substringBefore("://", missingDelimiterValue = "")
        if (scheme.isNotEmpty() && scheme !in config.allowedSchemes) {
            return Result.failure(IllegalArgumentException("Scheme '$scheme' not allowed"))
        }
        return Result.success(Unit)
    }
}