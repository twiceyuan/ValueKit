package com.twiceyuan.valuekit

import android.util.Log
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
    val rawValue = target.registry.read(target.kitItem.name, property.name, DataType::class)
    val finalValue = readInterceptor(rawValue)
    Logger.i {
        """
            |read action occurred
            |~~ Read ~~
            |[Name = ${target::class.simpleName}.${property.name}] 
            |[Type = ${DataType::class.simpleName}]
            |[Raw Value = $rawValue]
            |[Final Value = $finalValue]
            |
            |""".trimMargin()
    }
    return finalValue
}

inline operator fun <reified DataType : Any> ValueDelegate<DataType>.setValue(target: Any, property: KProperty<*>, newValue: DataType?) {
    // 只在日志打开时获取上一次的值
    val previousValue = Logger.exec { target.registry.read(target.kitItem.name, property.name, DataType::class) }
    val finalValue = writeInterceptor(newValue)
    target.registry.write(target.kitItem.name, property.name, writeInterceptor(newValue))
    Logger.i {
        """
            |write action occurred
            |~~ Write ~~
            |[Name = ${target::class.simpleName}.${property.name}] 
            |[Type = ${DataType::class.simpleName}]
            |[Previous = $previousValue]
            |[New Value = $newValue]
            |[Final Value = $finalValue]
            |
            |""".trimMargin()
    }
}

fun <T : Any> valueKit(
        readInterceptor: ValueInterceptor<T> = { it },
        writeValueInterceptor: ValueInterceptor<T> = { it }
) = ValueDelegate(readInterceptor, writeValueInterceptor)

/**
 * 用来标注一个 class/object 会被用来定义 ValueKit 条目
 *
 * [name] group name / tag，用来区分该文件下的 value 以什么标识进行区分，例如存储这组 value 的文件名
 * [registry]
 */
annotation class ValueKitItem(
        val name: String,
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
fun setupValueKitDefaultRegistry(registry: ValueKitRegistry) {
    Registry.defaultRegistry = registry
    Logger.i { "[Setup registry => ${registry::class.qualifiedName}]" }
}

object ValueKitLogger {
    var isEnable = false
    private const val DEFAULT_TAG = "ValueKitLogger"

    operator fun invoke(action: (defaultTag: String) -> Unit) {
        if (isEnable) {
            action(DEFAULT_TAG)
        }
    }

    fun <T> exec(action: () -> T): T? {
        return if (isEnable) {
            action()
        } else {
            null
        }
    }

    fun v(tr: Throwable? = null, m: () -> String) = exec { Log.v(DEFAULT_TAG, m(), tr) }
    fun d(tr: Throwable? = null, m: () -> String) = exec { Log.d(DEFAULT_TAG, m(), tr) }
    fun i(tr: Throwable? = null, m: () -> String) = exec { Log.i(DEFAULT_TAG, m(), tr) }
    fun w(tr: Throwable? = null, m: () -> String) = exec { Log.w(DEFAULT_TAG, m(), tr) }
    fun e(tr: Throwable? = null, m: () -> String) = exec { Log.e(DEFAULT_TAG, m(), tr) }
}

internal typealias Logger = ValueKitLogger