package org.igorv8836.bdui.logger

/**
 * Simple multiplatform console logger. Uses println with a level prefix.
 */
class ConsoleLogger(
    private val tag: String = "BDUI",
) : Logger {

    override fun debug(message: String, throwable: Throwable?) {
        emit("DEBUG", message, throwable)
    }

    override fun info(message: String, throwable: Throwable?) {
        emit("INFO", message, throwable)
    }

    override fun warn(message: String, throwable: Throwable?) {
        emit("WARN", message, throwable)
    }

    override fun error(message: String, throwable: Throwable?) {
        emit("ERROR", message, throwable)
    }

    private fun emit(level: String, message: String, throwable: Throwable?) {
        val prefix = "[$tag][$level]"
        if (throwable != null) {
            println("$prefix $message\n${throwable.stackTraceToString()}")
        } else {
            println("$prefix $message")
        }
    }
}
