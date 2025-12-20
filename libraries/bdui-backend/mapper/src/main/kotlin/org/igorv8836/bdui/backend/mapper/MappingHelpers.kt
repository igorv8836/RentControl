package org.igorv8836.bdui.backend.mapper

/**
 * Utility helpers for pure mapping functions.
 */
inline fun <I, O> mapList(
    source: Iterable<I>,
    crossinline mapper: (I) -> O,
): List<O> = source.map { mapper(it) }

inline fun <I, O> mapNotNull(
    source: Iterable<I?>,
    crossinline mapper: (I) -> O?,
): List<O> = source.mapNotNull { it?.let(mapper) }

inline fun <T> withDefault(
    value: T?,
    default: () -> T,
): T = value ?: default()
