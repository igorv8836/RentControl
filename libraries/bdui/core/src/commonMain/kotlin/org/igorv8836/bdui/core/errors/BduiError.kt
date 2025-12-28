package org.igorv8836.bdui.core.errors

/**
 * Unified error model for the client stack.
 */
sealed class BduiError(open val message: String, open val cause: Throwable? = null) {
    data class Network(
        val statusCode: Int,
        val payload: String,
        override val message: String = "Network error $statusCode",
        override val cause: Throwable? = null,
    ) : BduiError(message, cause)

    data class Remote(
        override val message: String,
        override val cause: Throwable? = null,
    ) : BduiError(message, cause)

    data class Decode(
        override val message: String,
        override val cause: Throwable? = null,
    ) : BduiError(message, cause)

    data class Validation(
        override val message: String,
        override val cause: Throwable? = null,
    ) : BduiError(message, cause)

    data class Unknown(
        override val message: String = "Unknown error",
        override val cause: Throwable? = null,
    ) : BduiError(message, cause)
}

class BduiException(val error: BduiError) : Exception(error.message, error.cause)
