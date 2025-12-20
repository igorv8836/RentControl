package org.igorv8836.bdui.backend.data

import org.igorv8836.bdui.backend.core.BackendError
import org.igorv8836.bdui.backend.core.BackendResult

/**
 * Abstraction for fetching data from any source (HTTP/DB/gRPC/files/etc).
 * Implementation is responsible for I/O, retries, timeouts, logging.
 */
interface DataProvider<Req, Res> {
    suspend fun fetch(request: Req, policy: DataPolicy = DataPolicy()): BackendResult<Res>
}

/**
 * Retry/timeout and logging hints for provider implementations.
 * Suitable for network, DB, or file I/O operations.
 */
data class DataPolicy(
    val operationTimeoutMillis: Long = 5_000,
    val maxRetries: Int = 0,
    val retryBackoffMillis: Long = 200,
    val retryOn: Set<RetryCondition> = emptySet(),
    val logRequests: Boolean = false,
    val logResponses: Boolean = false,
)

/**
 * Types of retryable conditions (network or storage).
 */
enum class RetryCondition {
    Network,
    Timeout,
    ServerError,
    TransientDb,
}

/**
 * Helper to wrap exceptions into a backend error inside provider.
 */
fun <T> dataFailure(message: String, cause: Throwable? = null): BackendResult<T> =
    BackendResult.failure(
        BackendError.Unknown(
            message = message,
            cause = cause?.message,
        ),
    )
