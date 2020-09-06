package com.twiceyuan.valuekit

import android.util.Base64
import android.util.Log
import com.twiceyuan.valuekit.registry.DefaultRegistry
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
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
        throw IllegalStateException("Default registry not setup, please call the setupValueKitDefaultRegistry to set a default registry.")
    }
}

fun ValueKitRegistry.throwNotSupportDataType(kClass: KClass<*>): Nothing =
        throw UnsupportedOperationException("This type is unsupported: ${kClass.qualifiedName}")

inline fun <reified Type> KClass<*>.isAssignableFrom() = Type::class.java.isAssignableFrom(java)

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
    target.registry.write(target.kitItem.name, property.name, writeInterceptor(newValue), DataType::class)
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
 * Value 序列化和反序列的接口，实现该接口可作为一个 ValueKit 的序列化实现
 */
interface ValueKitRegistry {

    /**
     * 读取一个值
     * [Data] 值的类型
     * [group] 该值对应的 group 分组，一般对应 [ValueKitItem.name]
     * [dataType] 数据的 KClass 类型
     */
    fun <Data : Any> read(group: String, propertyName: String, dataType: KClass<out Data>): Data?

    /**
     * 写入一个值
     *
     * [group] 该值对应的 group 分组，一般对应 [ValueKitItem.name]
     */
    fun write(group: String, propertyName: String, newValue: Any?, dataType: KClass<*>)
}

internal object Registry {
    var defaultRegistry: ValueKitRegistry? = null
    val cache = mutableMapOf<KClass<out ValueKitRegistry>, ValueKitRegistry?>()
}

fun ValueKitRegistry.deserializeObj(base64: String?): Any? {
    base64 ?: return null
    return base64.runCatching {
        val b = Base64.decode(this.toByteArray(), Base64.URL_SAFE)
        val bi = ByteArrayInputStream(b)
        val si = ObjectInputStream(bi)
        si.readObject()
    }.onFailure { e ->
        ValueKitLogger { Log.e(it, e.message, e) }
    }.getOrNull()
}

fun ValueKitRegistry.serializeObj(obj: Any): String? {
    return obj.runCatching {
        val bo = ByteArrayOutputStream()
        val so = ObjectOutputStream(bo)
        so.writeObject(this)
        so.flush()
        Base64.encodeToString(bo.toByteArray(), Base64.URL_SAFE)
    }.onFailure { e ->
        ValueKitLogger { Log.e(it, e.message, e) }
    }.getOrNull()
}

/**
 * 配置默认 ValueKitRegistry
 */
fun setupValueKitDefaultRegistry(registry: ValueKitRegistry) {
    Registry.defaultRegistry = registry
    Logger.i { "[Setup registry => ${registry::class.qualifiedName}]" }
}