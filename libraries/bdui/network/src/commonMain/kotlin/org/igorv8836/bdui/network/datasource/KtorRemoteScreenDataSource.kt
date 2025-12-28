package org.igorv8836.bdui.network.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.core.errors.BduiError
import org.igorv8836.bdui.core.errors.BduiException
import org.igorv8836.bdui.logger.ConsoleLogger
import org.igorv8836.bdui.logger.Logger
import org.igorv8836.bdui.logger.LogMessages
import org.igorv8836.bdui.logger.LogTags
import org.igorv8836.bdui.logger.formatLog
import org.igorv8836.bdui.network.client.createHttpClient
import org.igorv8836.bdui.network.config.NetworkConfig

/**
 * Ktor-based implementation that delegates decoding to provided function.
 * Keeps contract models free from serialization annotations while providing robust networking.
 */
class KtorRemoteScreenDataSource(
    private val config: NetworkConfig,
    private val json: Json = Json { ignoreUnknownKeys = true },
    private val decode: suspend (String) -> RemoteScreen = { body ->
        json.decodeFromString(RemoteScreen.serializer(), body)
    },
    client: HttpClient? = null,
    private val logger: Logger = ConsoleLogger(LogTags.NET),
) : RemoteScreenDataSource {

    private val httpClient: HttpClient = client ?: createHttpClient(config) { message ->
        logger.debug(message)
    }

    override suspend fun fetch(screenId: String, params: Map<String, String>): Result<RemoteScreen> = runCatching {
        logger.info(formatLog(LogMessages.FETCH_SCREEN, screenId, params))
        val response = httpClient.get {
            url {
                takeFrom(config.baseUrl)
                val cleanSegments = screenId.trim('/').split('/').filter { it.isNotBlank() }
                if (cleanSegments.isNotEmpty()) {
                    appendPathSegments(cleanSegments)
                }
                params.forEach { (key, value) -> parameter(key, value) }
            }
            config.defaultHeaders.forEach { (key, value) -> headers.append(key, value) }
        }

        if (!response.status.isSuccess()) {
            val body = response.safeBody()
            logger.warn(formatLog(LogMessages.NETWORK_ERROR, response.status.value, body))
            throw BduiException(BduiError.Network(response.status.value, body))
        }
        val body = response.safeBody()
        decode(body)
    }.recoverCatching { throwable ->
        val error = when (throwable) {
            is BduiException -> throwable.error
            else -> BduiError.Remote("Failed to load screen '$screenId'", throwable)
        }
        logger.error(formatLog(LogMessages.REMOTE_ERROR, screenId, error.message), throwable)
        throw BduiException(error)
    }

    private suspend fun HttpResponse.safeBody(): String = withContext(Dispatchers.Default) {
        runCatching { bodyAsText() }.getOrDefault("")
    }
}
