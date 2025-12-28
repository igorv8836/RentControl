package org.igorv8836.bdui.logger

/**
 * Fan-out logger that delegates each call to all provided loggers.
 */
class CompositeLogger(
    private val delegates: List<Logger>,
) : Logger {

    override fun debug(message: String, throwable: Throwable?) {
        delegates.forEach { it.debug(message, throwable) }
    }

    override fun info(message: String, throwable: Throwable?) {
        delegates.forEach { it.info(message, throwable) }
    }

    override fun warn(message: String, throwable: Throwable?) {
        delegates.forEach { it.warn(message, throwable) }
    }

    override fun error(message: String, throwable: Throwable?) {
        delegates.forEach { it.error(message, throwable) }
    }
}
