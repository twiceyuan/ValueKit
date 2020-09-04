package com.twiceyuan.valuekit

import java.io.Serializable
import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by twiceYuan on 2017/12/6.
 */

typealias ValueInterceptor<T> = (originValue: T?) -> T?

class ValueDelegate<DataType>(
        private val readInterceptor: ValueInterceptor<DataType>,
        private val writeInterceptor: ValueInterceptor<DataType>
) {

    private val Any.kitItem: ValueKitItem
        get() = this::class.java.getAnnotation(ValueKitItem::class.java) ?: throwNotSetupRegistry()

    private val Any.registry: ValueKitRegistry
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

    operator fun getValue(target: Any, property: KProperty<*>): DataType? {
        return readInterceptor(target.registry.read(target.kitItem.name, property.name))
    }

    operator fun setValue(target: Any, property: KProperty<*>, newValue: DataType?) {
        return target.registry.write(target.kitItem.name, property.name, writeInterceptor(newValue))
    }
}

fun intValue(
        readInterceptor: ValueInterceptor<Int> = { it },
        writeValueInterceptor: ValueInterceptor<Int> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

fun longValue(
        readInterceptor: ValueInterceptor<Long> = { it },
        writeValueInterceptor: ValueInterceptor<Long> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

fun byteValue(
        readInterceptor: ValueInterceptor<Byte> = { it },
        writeValueInterceptor: ValueInterceptor<Byte> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

fun booleanValue(
        readInterceptor: ValueInterceptor<Boolean> = { it },
        writeValueInterceptor: ValueInterceptor<Boolean> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

fun floatValue(
        readInterceptor: ValueInterceptor<Float> = { it },
        writeValueInterceptor: ValueInterceptor<Float> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

fun doubleValue(
        readInterceptor: ValueInterceptor<Double> = { it },
        writeValueInterceptor: ValueInterceptor<Double> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

fun stringValue(
        readInterceptor: ValueInterceptor<String> = { it },
        writeValueInterceptor: ValueInterceptor<String> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

fun <T : Serializable> objectValue(
        readInterceptor: ValueInterceptor<T> = { it },
        writeValueInterceptor: ValueInterceptor<T> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

annotation class ValueKitItem(
        val name: String, //
        val registry: KClass<out ValueKitRegistry> = DefaultRegistry::class
)
