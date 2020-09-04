package com.twiceyuan.valuekit

import android.util.Log
import java.io.*
import kotlin.reflect.KClass

private const val TAG = "ValueRegistry"

/**
 * 值拦截器，可对值进行一层拦截
 */
typealias ValueInterceptor<T> = (T?) -> T?

interface ValueKitRegistry {

    fun <Data : Any> read(group: String, propertyName: String, interceptor: ValueInterceptor<Data>): Data?

    fun write(group: String, propertyName: String, newValue: Any?, interceptor: ValueInterceptor<*>)
}

/**
 * 简单数据输入输出 registry
 */
class SimpleInputOutputRegistry : ValueKitRegistry {

    private val file: File = Initializer.context.filesDir

    private fun valueDir(dirName: String? = null): File =
            File(file, dirName ?: DEFAULT_DIR_NAME).apply { mkdirs() }

    override fun <Data : Any> read(
            group: String,
            propertyName: String,
            interceptor: ValueInterceptor<Data>): Data? {

        return runCatching {
            val file = File(valueDir(group), propertyName).inputStream()
            @Suppress("UNCHECKED_CAST")
            ObjectInputStream(file).readObject() as? Data
        }.onFailure {
            Log.e(TAG, it.message, it)
        }.getOrNull()
    }

    override fun write(group: String, propertyName: String, newValue: Any?, interceptor: ValueInterceptor<*>) {
        File(valueDir(group), propertyName).apply {
            createNewFile()
            ObjectOutputStream(FileOutputStream(this)).writeObject(newValue)
        }
    }

    companion object {
        const val DEFAULT_DIR_NAME = "value_kit"
    }
}

internal lateinit var defaultValueKitRegistry: ValueKitRegistry

internal val valueKitRegistries = mutableMapOf<KClass<out ValueKitRegistry>, ValueKitRegistry?>()

/**
 * 配置默认 ValueKitRegistry
 */
fun setupValueKitDefaultRegister(registry: ValueKitRegistry) {
    defaultValueKitRegistry = registry
}