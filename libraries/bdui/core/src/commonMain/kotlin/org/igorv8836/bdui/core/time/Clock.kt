package org.igorv8836.bdui.core.time

/**
 * Platform clock abstraction to keep shared code free from platform APIs.
 */
expect fun currentTimeMillis(): Long
