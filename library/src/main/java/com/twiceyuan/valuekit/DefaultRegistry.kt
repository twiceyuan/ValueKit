package com.twiceyuan.valuekit

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * 简单数据输入输出 registry
 */
class DefaultRegistry(context: Context) : ValueKitRegistry {

    private val file: File = context.filesDir

    private fun valueDir(dirName: String? = null): File =
            File(file, dirName ?: DEFAULT_DIR_NAME).apply { mkdirs() }

    override fun <Data : Any> read(group: String, propertyName: String): Data? {
        return runCatching {
            val file = File(valueDir(group), propertyName).inputStream()
            @Suppress("UNCHECKED_CAST")
            ObjectInputStream(file).readObject() as? Data
        }.onFailure {
            Log.v(TAG, it.message, it)
        }.getOrNull()
    }

    override fun write(group: String, propertyName: String, newValue: Any?) {
        File(valueDir(group), propertyName).apply {
            createNewFile()
            ObjectOutputStream(FileOutputStream(this)).writeObject(newValue)
        }
    }

    companion object {
        private const val DEFAULT_DIR_NAME = "value_kit"
        private const val TAG = "JavaObjectRegistry"
    }
}