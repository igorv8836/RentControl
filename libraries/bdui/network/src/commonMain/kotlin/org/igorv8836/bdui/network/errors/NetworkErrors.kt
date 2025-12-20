package org.igorv8836.bdui.network.errors

class ScreenNetworkException(
    val statusCode: Int,
    val payload: String,
) : Exception("Screen request failed: $statusCode, payload=$payload")

class ScreenRemoteException(message: String, cause: Throwable) : Exception(message, cause)
