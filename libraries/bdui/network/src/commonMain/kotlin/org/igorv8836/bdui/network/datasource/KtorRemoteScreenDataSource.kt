package org.igorv8836.bdui.network.datasource

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import org.igorv8836.bdui.contract.RemoteScreen
import org.igorv8836.bdui.network.client.createHttpClient
import org.igorv8836.bdui.network.config.NetworkConfig
import org.igorv8836.bdui.network.errors.ScreenNetworkException
import org.igorv8836.bdui.network.errors.ScreenRemoteException

/**
 * Ktor-based implementation that delegates decoding to provided function.
 * Keeps contract models free from serialization annotations while providing robust networking.
 */
class KtorRemoteScreenDataSource(
    private val config: NetworkConfig,
    private val decode: suspend (String) -> RemoteScreen,
    client: HttpClient? = null,
    private val logger: (String) -> Unit = {},
) : RemoteScreenDataSource {

    private val httpClient: HttpClient = client ?: createHttpClient(config, logger)

    override suspend fun fetch(screenId: String, params: Map<String, String>): Result<RemoteScreen> = runCatching {
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
            throw ScreenNetworkException(response.status.value, body)
        }
        val body = response.safeBody()
        decode(body)
    }.recoverCatching { throwable ->
        throw ScreenRemoteException("Failed to load screen '$screenId'", throwable)
    }

    private suspend fun HttpResponse.safeBody(): String = runCatching { bodyAsText() }.getOrDefault("")
}
