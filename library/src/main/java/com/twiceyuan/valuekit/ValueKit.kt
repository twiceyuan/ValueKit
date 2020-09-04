package com.twiceyuan.valuekit

import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by twiceYuan on 2017/12/6.
 */
abstract class ValueDelegate<DataType>(
        private val readInterceptor: ValueInterceptor<DataType>,
        private val writeValueInterceptor: ValueInterceptor<DataType>
) {

    private val Any.valueKitItem: ValueKitItem
        get() = this::class.java.getAnnotation(ValueKitItem::class.java)

    private val Any.registry: ValueKitRegistry
        get() {
            val annotationClass = valueKitItem.registry
            val registryInstance = valueKitRegistries[annotationClass]
            if (registryInstance != null) {
                return registryInstance
            }

            synchronized(annotationClass.java) {
                val instance = annotationClass.java.newInstance()
                valueKitRegistries[annotationClass] = instance
                return instance
            }
        }

    operator fun getValue(any: Any, property: KProperty<*>): DataType? {
        return readInterceptor(any.registry.read(any.valueKitItem.name, property.name, readInterceptor))
    }

    operator fun setValue(any: Any, property: KProperty<*>, newValue: DataType?) {
        return any.registry.write(any.valueKitItem.name, property.name, writeValueInterceptor(newValue), writeValueInterceptor)
    }
}

fun intValue(
        readInterceptor: ValueInterceptor<Int> = { it },
        writeValueInterceptor: ValueInterceptor<Int> = { it }
) = IntegerValue(readInterceptor, writeValueInterceptor)

fun longValue(
        readInterceptor: ValueInterceptor<Long> = { it },
        writeValueInterceptor: ValueInterceptor<Long> = { it }
) = LongValue(readInterceptor, writeValueInterceptor)

fun byteValue(
        readInterceptor: ValueInterceptor<Byte> = { it },
        writeValueInterceptor: ValueInterceptor<Byte> = { it }
) = ByteValue(readInterceptor, writeValueInterceptor)

fun booleanValue(
        readInterceptor: ValueInterceptor<Boolean> = { it },
        writeValueInterceptor: ValueInterceptor<Boolean> = { it }
) = BooleanValue(readInterceptor, writeValueInterceptor)

fun floatValue(
        readInterceptor: ValueInterceptor<Float> = { it },
        writeValueInterceptor: ValueInterceptor<Float> = { it }
) = FloatValue(readInterceptor, writeValueInterceptor)

fun doubleValue(
        readInterceptor: ValueInterceptor<Double> = { it },
        writeValueInterceptor: ValueInterceptor<Double> = { it }
) = DoubleValue(readInterceptor, writeValueInterceptor)

fun stringValue(
        readInterceptor: ValueInterceptor<String> = { it },
        writeValueInterceptor: ValueInterceptor<String> = { it }
) = StringValue(readInterceptor, writeValueInterceptor)

fun <T: Serializable> objectValue(
        readInterceptor: ValueInterceptor<T> = { it },
        writeValueInterceptor: ValueInterceptor<T> = { it }
) = ObjectValue(readInterceptor, writeValueInterceptor)

private typealias Interceptor<T> = ValueInterceptor<T>

class IntegerValue(i1: Interceptor<Int>, i2: Interceptor<Int>) : ValueDelegate<Int>(i1, i2)

class LongValue(i1: Interceptor<Long>, i2: Interceptor<Long>) : ValueDelegate<Long>(i1, i2)

class ByteValue(i1: Interceptor<Byte>, i2: Interceptor<Byte>) : ValueDelegate<Byte>(i1, i2)

class BooleanValue(i1: Interceptor<Boolean>, i2: Interceptor<Boolean>) : ValueDelegate<Boolean>(i1, i2)

class FloatValue(i1: Interceptor<Float>, i2: Interceptor<Float>) : ValueDelegate<Float>(i1, i2)

class DoubleValue(i1: Interceptor<Double>, i2: Interceptor<Double>) : ValueDelegate<Double>(i1, i2)

class StringValue(i1: Interceptor<String>, i2: Interceptor<String>) : ValueDelegate<String>(i1, i2)

@Suppress("unused")
class ObjectValue<Obj: Serializable>(i1: ValueInterceptor<Obj>, i2: ValueInterceptor<Obj>) : ValueDelegate<Obj>(i1, i2)

annotation class ValueKitItem(
        val name: String, //
        val registry: KClass<out ValueKitRegistry> = SimpleInputOutputRegistry::class
)
