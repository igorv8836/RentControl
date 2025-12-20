package org.igorv8836.bdui.common.time

/**
 * Platform clock abstraction to keep shared code free from platform APIs.
 */
expect fun currentTimeMillis(): Long
