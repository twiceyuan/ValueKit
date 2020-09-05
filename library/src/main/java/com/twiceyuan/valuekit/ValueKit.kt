package com.twiceyuan.valuekit

import com.twiceyuan.valuekit.registry.DefaultRegistry
import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by twiceYuan on 2017/12/6.
 */

typealias ValueInterceptor<T> = (originValue: T?) -> T?

class ValueDelegate<DataType>(
        val readInterceptor: ValueInterceptor<DataType>,
        val writeInterceptor: ValueInterceptor<DataType>
) {

    val Any.kitItem: ValueKitItem
        get() = this::class.java.getAnnotation(ValueKitItem::class.java) ?: throwNotSetupRegistry()

    val Any.registry: ValueKitRegistry
        get() {
            val annotationClass = kitItem.registry

            if (annotationClass == DefaultRegistry::class) {
                val defaultRegistry = Registry.defaultRegistry
                return defaultRegistry ?: throwNotSetupDefaultRegistry()
            }

            val registryInstance = Registry.cache[annotationClass]
            if (registryInstance != null) {
                return registryInstance
            }

            synchronized(annotationClass.java) {
                val instance = annotationClass.java.newInstance()
                Registry.cache[annotationClass] = instance
                return instance
            }
        }

    private fun throwNotSetupRegistry(): Nothing {
        throw IllegalStateException("Registry not setup")
    }

    private fun throwNotSetupDefaultRegistry(): Nothing {
        throw IllegalStateException("Default registry not setup, please call the setupValueKitDefaultRegister to set a default registry.")
    }
}

inline operator fun <reified DataType : Any> ValueDelegate<DataType>.getValue(target: Any, property: KProperty<*>): DataType? {
    return readInterceptor(target.registry.read(target.kitItem.name, property.name, DataType::class))
}

operator fun <DataType> ValueDelegate<DataType>.setValue(target: Any, property: KProperty<*>, newValue: DataType?) {
    return target.registry.write(target.kitItem.name, property.name, writeInterceptor(newValue))
}

fun <T : Any> valueKit(
        readInterceptor: ValueInterceptor<T> = { it },
        writeValueInterceptor: ValueInterceptor<T> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

annotation class ValueKitItem(
        val name: String, //
        val registry: KClass<out ValueKitRegistry> = DefaultRegistry::class
)

/**
 * 值拦截器，可对值进行一层拦截
 */
interface ValueKitRegistry {

    fun <Data : Any> read(group: String, propertyName: String, kClass: KClass<out Data>): Data?

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