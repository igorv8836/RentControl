package org.igorv8836.bdui.backend.core

/**
 * Typed result for backend pipeline (mapping/validation/rendering).
 */
sealed class BackendResult<out T> {
    data class Success<T>(val value: T) : BackendResult<T>()
    data class Failure(val error: BackendError) : BackendResult<Nothing>()

    inline fun <R> map(transform: (T) -> R): BackendResult<R> =
        when (this) {
            is Success -> Success(transform(value))
            is Failure -> this
        }

    inline fun onSuccess(block: (T) -> Unit): BackendResult<T> =
        also { if (this is Success) block(value) }

    inline fun onFailure(block: (BackendError) -> Unit): BackendResult<T> =
        also { if (this is Failure) block(error) }

    companion object {
        fun <T> success(value: T): BackendResult<T> = Success(value)
        fun failure(error: BackendError): BackendResult<Nothing> = Failure(error)
    }
}

/**
 * Unified error model for backend steps.
 */
sealed class BackendError {
    abstract val message: String

    data class Validation(
        override val message: String,
        val issues: List<ValidationIssue>,
    ) : BackendError()

    data class Mapping(
        override val message: String,
        val cause: String? = null,
    ) : BackendError()

    data class Serialization(
        override val message: String,
        val cause: String? = null,
    ) : BackendError()

    data class LimitExceeded(
        override val message: String,
        val limit: String,
        val value: Int? = null,
        val threshold: Int? = null,
    ) : BackendError()

    data class Unknown(
        override val message: String,
        val cause: String? = null,
    ) : BackendError()
}

data class ValidationIssue(
    val path: String,
    val message: String,
    val code: String? = null,
)
