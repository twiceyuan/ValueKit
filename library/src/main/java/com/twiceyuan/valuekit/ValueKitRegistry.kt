package com.twiceyuan.valuekit

import kotlin.reflect.KClass

/**
 * 值拦截器，可对值进行一层拦截
 */
interface ValueKitRegistry {

    fun <Data : Any> read(group: String, propertyName: String): Data?

    fun write(group: String, propertyName: String, newValue: Any?)
}

internal object Registry {
    var defaultRegistry: ValueKitRegistry? = null
    val cache = mutableMapOf<KClass<out ValueKitRegistry>, ValueKitRegistry?>()
}

/**
 * 配置默认 ValueKitRegistry
 */
fun setupValueKitDefaultRegister(registry: ValueKitRegistry) {
    Registry.defaultRegistry = registry
}