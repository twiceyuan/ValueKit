package com.twiceyuan.valuekit.registry

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.twiceyuan.valuekit.ValueKitLogger
import com.twiceyuan.valuekit.ValueKitRegistry
import java.io.*
import kotlin.reflect.KClass

/**
 * 使用 SharedPreferences 持久化数据存储的实现
 *
 * 其中 Int、Long、Byte、Double、Float、String 均会使用 string 进行存储，以消解 sp 在原始类型默认值上的歧义性
 * StringSet 使用 sp 原始支持进行存储
 * Serializable 使用 java 的 I/O stream 进行 base64 后存储。
 */
class SharedPreferencesRegistry(val context: Context) : ValueKitRegistry {

    inner class Preferences {
        operator fun get(name: String): SharedPreferences =
                context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    private val preferences = Preferences()

    private inline fun <reified Type> KClass<*>.isAssignableFrom() =
            Type::class.java.isAssignableFrom(java)

    override fun <Data : Any> read(group: String, propertyName: String, kClass: KClass<out Data>): Data? {

        val preference = preferences[group]

        // 因为 sp 不能无歧义的处理原生类型的默认值问题，因此这些原生类型按照 string 存储并转换
        fun stringValue() = preference.getString(propertyName, null)

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        return when {
            kClass.isAssignableFrom<Int>() -> stringValue()?.toInt()
            kClass.isAssignableFrom<Long>() -> stringValue()?.toLong()
            kClass.isAssignableFrom<Byte>() -> stringValue()?.toByte()
            kClass.isAssignableFrom<Double>() -> stringValue()?.toDouble()
            kClass.isAssignableFrom<Float>() -> stringValue()?.toFloat()
            kClass.isAssignableFrom<String>() -> stringValue()
            kClass.isStringSetType -> preference.getStringSet(propertyName, null)
            kClass.isAssignableFrom<Serializable>() -> stringValue()?.deserializeObj()
            else -> throwNotSupportDataType(kClass)
        } as? Data?
    }

    private fun String.deserializeObj(): Any? {
        return runCatching {
            val b = Base64.decode(toByteArray(), Base64.URL_SAFE)
            val bi = ByteArrayInputStream(b)
            val si = ObjectInputStream(bi)
            si.readObject()
        }.onFailure { e ->
            ValueKitLogger { Log.e(it, e.message, e) }
        }.getOrNull()
    }

    private fun Any.serializeObj(): String? {
        return runCatching {
            val bo = ByteArrayOutputStream()
            val so = ObjectOutputStream(bo)
            so.writeObject(this)
            so.flush()
            Base64.encodeToString(bo.toByteArray(), Base64.URL_SAFE)
        }.onFailure { e ->
            ValueKitLogger { Log.e(it, e.message, e) }
        }.getOrNull()
    }

    override fun write(group: String, propertyName: String, newValue: Any?) {
        val preference = preferences[group]

        if (newValue == null) {
            preference.edit().remove(propertyName).apply()
            return
        }

        fun writeValue(editor: SharedPreferences.Editor.() -> Unit) {
            val preferenceEditor = preference.edit()
            preferenceEditor.editor()
            preferenceEditor.apply()
        }

        val kClass = newValue::class

        when {
            kClass.isStoreByStringType -> writeValue {
                putString(propertyName, newValue.toString())
            }
            kClass.isStringSetType -> writeValue {
                @Suppress("UNCHECKED_CAST")
                putStringSet(propertyName, newValue as Set<String>)
            }
            kClass.isAssignableFrom<Serializable>() -> writeValue {
                putString(propertyName, newValue.serializeObj())
            }
            else -> throwNotSupportDataType(kClass)
        }
    }

    private val KClass<*>.isStoreByStringType: Boolean
        get() {
            return listOf(
                    isAssignableFrom<Int>(),
                    isAssignableFrom<Long>(),
                    isAssignableFrom<Byte>(),
                    isAssignableFrom<Double>(),
                    isAssignableFrom<Float>(),
                    isAssignableFrom<String>()
            ).any { it }
        }

    private val KClass<*>.isStringSetType: Boolean
        get() {
            if (!isAssignableFrom<Set<*>>()) {
                return false
            }
            val typeParam = typeParameters.getOrNull(0) as? KClass<*>
            return typeParam?.isAssignableFrom<String>() == true
        }

    private fun throwNotSupportDataType(kClass: KClass<*>): Nothing =
            throw UnsupportedOperationException("This type is unsupported: ${kClass.qualifiedName}")
}