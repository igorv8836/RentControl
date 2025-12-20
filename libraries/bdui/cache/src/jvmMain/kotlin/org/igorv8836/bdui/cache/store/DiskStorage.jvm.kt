package org.igorv8836.bdui.cache.store

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class DiskStorage actual constructor(basePath: String) {
    private val dir = File(basePath).apply { mkdirs() }

    private fun fileFor(key: String): File = File(dir, sanitize(key))

    private fun sanitize(key: String): String = key.replace(Regex("[^A-Za-z0-9._-]"), "_")

    actual suspend fun write(key: String, content: String) {
        withContext(Dispatchers.IO) {
            fileFor(key).writeText(content, Charsets.UTF_8)
        }
    }

    actual suspend fun read(key: String): String? =
        withContext(Dispatchers.IO) {
            val file = fileFor(key)
            if (file.exists()) file.readText(Charsets.UTF_8) else null
        }

    actual suspend fun delete(key: String) {
        withContext(Dispatchers.IO) {
            fileFor(key).delete()
        }
    }

    actual suspend fun listKeys(): List<String> =
        withContext(Dispatchers.IO) {
            dir.list()?.toList().orEmpty()
        }
}
