package org.igorv8836.bdui.logger

object LogTags {
    const val ENGINE = "BDUI-ENGINE"
    const val NET = "BDUI-NET"
    const val RUNTIME = "BDUI-RUNTIME"
    const val ACTIONS = "BDUI-ACTIONS"
}

object LogMessages {
    const val FETCH_SCREEN = "fetch screen=%s params=%s"
    const val NETWORK_ERROR = "network error status=%s body=%s"
    const val REMOTE_ERROR = "remote error for screen=%s: %s"
    const val LOAD_START = "load screen=%s params=%s"
    const val LOAD_SUCCESS = "loaded screen=%s"
    const val LOAD_FAIL = "load failed screen=%s"
    const val REFRESH_START = "refresh screen=%s params=%s"
    const val REFRESH_SUCCESS = "refresh success screen=%s"
    const val REFRESH_FAIL = "refresh failed screen=%s"
    const val NEXT_PAGE_START = "loadNextPage screen=%s page=%s"
    const val NEXT_PAGE_SUCCESS = "loadNextPage success screen=%s mergedEmpty=%s"
    const val NEXT_PAGE_FAIL = "loadNextPage failed screen=%s"
    const val DISPATCH_ACTION = "dispatch action=%s screen=%s"
    const val CONTROLLER_REFRESH = "controller refresh screen=%s params=%s"
    const val CONTROLLER_NEXT_PAGE = "controller loadNextPage screen=%s"
    const val ENGINE_CREATE = "create engine for screen=%s"
    const val MISSING_HANDLER = "No handler for action=%s id=%s"
    const val HANDLER_MISMATCH = "Handler type mismatch for action=%s"
}

/**
    * Lightweight formatter for %s placeholders to keep strings as constants.
    */
fun formatLog(pattern: String, vararg args: Any?): String {
    val parts = pattern.split("%s")
    if (parts.size == 1) return pattern
    val builder = StringBuilder()
    parts.forEachIndexed { index, part ->
        builder.append(part)
        if (index < args.size) {
            builder.append(args[index].toString())
        }
    }
    return builder.toString()
}
