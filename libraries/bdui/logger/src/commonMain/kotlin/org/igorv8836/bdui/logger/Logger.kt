package org.igorv8836.bdui.logger

/**
 * Lightweight logging facade used across the library.
 */
interface Logger {
    fun debug(message: String, throwable: Throwable? = null)
    fun info(message: String, throwable: Throwable? = null)
    fun warn(message: String, throwable: Throwable? = null)
    fun error(message: String, throwable: Throwable? = null)
}
