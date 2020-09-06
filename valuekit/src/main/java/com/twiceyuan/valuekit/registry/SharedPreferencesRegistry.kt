package com.twiceyuan.valuekit.registry

import android.content.Context
import android.content.SharedPreferences
import com.twiceyuan.valuekit.*
import java.io.Serializable
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

    override fun <Data : Any> read(group: String, propertyName: String, dataType: KClass<out Data>): Data? {

        val preference = preferences[group]

        // 因为 sp 不能无歧义的处理原生类型的默认值问题，因此这些原生类型按照 string 存储并转换
        fun stringValue() = preference.getString(propertyName, null)

        @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
        return when {
            dataType.isAssignableFrom<Int>() -> stringValue()?.toInt()
            dataType.isAssignableFrom<Long>() -> stringValue()?.toLong()
            dataType.isAssignableFrom<Byte>() -> stringValue()?.toByte()
            dataType.isAssignableFrom<Double>() -> stringValue()?.toDouble()
            dataType.isAssignableFrom<Float>() -> stringValue()?.toFloat()
            dataType.isAssignableFrom<String>() -> stringValue()
            dataType.isStringSetType -> preference.getStringSet(propertyName, null)
            dataType.isAssignableFrom<Serializable>() -> deserializeObj(stringValue() ?: "")
            else -> throwNotSupportDataType(dataType)
        } as? Data?
    }

    override fun write(group: String, propertyName: String, newValue: Any?, dataType: KClass<*>) {
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

        when {
            dataType.isStoreByStringType -> writeValue {
                putString(propertyName, newValue.toString())
            }
            dataType.isStringSetType -> writeValue {
                @Suppress("UNCHECKED_CAST")
                putStringSet(propertyName, newValue as Set<String>)
            }
            dataType.isAssignableFrom<Serializable>() -> writeValue {
                putString(propertyName, serializeObj(newValue))
            }
            else -> throwNotSupportDataType(dataType)
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
}