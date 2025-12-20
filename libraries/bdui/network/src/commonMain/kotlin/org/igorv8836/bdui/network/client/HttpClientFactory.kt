package org.igorv8836.bdui.network.client

import io.ktor.client.HttpClient
import org.igorv8836.bdui.network.config.NetworkConfig

internal expect fun createHttpClient(config: NetworkConfig, logger: (String) -> Unit): HttpClient
