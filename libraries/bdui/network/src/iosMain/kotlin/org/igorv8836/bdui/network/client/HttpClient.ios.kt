package org.igorv8836.bdui.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import org.igorv8836.bdui.network.config.NetworkConfig

internal actual fun createHttpClient(config: NetworkConfig, logger: (String) -> Unit): HttpClient =
    HttpClient(Darwin) {
        install(HttpTimeout) {
            requestTimeoutMillis = config.requestTimeoutMillis
            connectTimeoutMillis = config.connectTimeoutMillis
            socketTimeoutMillis = config.socketTimeoutMillis
        }
        install(Logging) {
            level = LogLevel.INFO
            logger(object : Logger {
                override fun log(message: String) {
                    logger(message)
                }
            })
        }
        expectSuccess = false
    }
