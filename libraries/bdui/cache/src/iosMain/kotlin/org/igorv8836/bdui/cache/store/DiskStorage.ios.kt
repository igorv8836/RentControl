package org.igorv8836.bdui.cache.store

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

actual class DiskStorage actual constructor(basePath: String) {
    private val dirPath: String = buildString {
        val root = if (basePath.startsWith("/")) basePath else NSHomeDirectory() + "/$basePath"
        append(root)
    }

    private fun sanitize(key: String): String = key.replace(Regex("[^A-Za-z0-9._-]"), "_")

    private fun filePath(key: String): String = "$dirPath/${sanitize(key)}"

    actual suspend fun write(key: String, content: String) {
        withContext(Dispatchers.Default) {
            NSFileManager.defaultManager.createDirectoryAtPath(dirPath, true, null, null)
            NSString.create(string = content).writeToFile(filePath(key), atomically = true, encoding = NSUTF8StringEncoding, error = null)
        }
    }

    actual suspend fun read(key: String): String? =
        withContext(Dispatchers.Default) {
            NSString.stringWithContentsOfFile(filePath(key), encoding = NSUTF8StringEncoding, error = null) as String?
        }

    actual suspend fun delete(key: String) {
        withContext(Dispatchers.Default) {
            NSFileManager.defaultManager.removeItemAtPath(filePath(key), error = null)
        }
    }

    actual suspend fun listKeys(): List<String> =
        withContext(Dispatchers.Default) {
            NSFileManager.defaultManager.contentsOfDirectoryAtPath(dirPath, error = null)?.map { it.toString() } ?: emptyList()
        }
}
