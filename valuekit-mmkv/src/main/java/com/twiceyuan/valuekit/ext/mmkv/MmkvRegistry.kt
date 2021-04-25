package com.twiceyuan.valuekit.ext.mmkv

import android.os.Parcelable
import com.tencent.mmkv.MMKV
import com.twiceyuan.valuekit.*
import java.io.Serializable
import kotlin.reflect.KClass

class MmkvRegistry : ValueKitRegistry {

    override fun <Data : Any> read(group: String, propertyName: String, dataType: KClass<out Data>): Data? {
        val repo = MMKV.mmkvWithID(group)
        return dataType.run {
            @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
            when {
                isAssignableFrom<Boolean>() -> repo.decodeBool(propertyName)
                isAssignableFrom<Int>() -> repo.decodeInt(propertyName)
                isAssignableFrom<Long>() -> repo.decodeLong(propertyName)
                isAssignableFrom<Float>() -> repo.decodeFloat(propertyName)
                isAssignableFrom<Double>() -> repo.decodeDouble(propertyName)
                isAssignableFrom<String>() -> repo.decodeString(propertyName)
                isAssignableFrom<Array<Byte>>() -> repo.decodeBytes(propertyName)
                isAssignableFrom<Parcelable>() -> repo.decodeParcelable(propertyName, dataType.java as Class<out Parcelable>)
                isStringSetType -> repo.decodeStringSet(propertyName)
                isAssignableFrom<Serializable>() -> deserializeObj(repo.decodeString(propertyName))
                else -> throwNotSupportDataType(dataType)
            } as Data?
        }
    }

    override fun write(group: String, propertyName: String, newValue: Any?, dataType: KClass<*>) {
        val repo = MMKV.mmkvWithID(group)
        if (newValue == null) {
            repo.removeValueForKey(propertyName)
            return
        }
        dataType.apply {
            when {
                isAssignableFrom<Boolean>() -> repo.encode(propertyName, newValue as Boolean)
                isAssignableFrom<Int>() -> repo.encode(propertyName, newValue as Int)
                isAssignableFrom<Long>() -> repo.encode(propertyName, newValue as Long)
                isAssignableFrom<Float>() -> repo.encode(propertyName, newValue as Float)
                isAssignableFrom<Double>() -> repo.encode(propertyName, newValue as Double)
                isAssignableFrom<String>() -> repo.encode(propertyName, newValue as String)
                isAssignableFrom<Array<Byte>>() -> repo.encode(propertyName, newValue as ByteArray)
                isAssignableFrom<Parcelable>() -> repo.encode(propertyName, newValue as Parcelable)
                isStringSetType -> {
                    @Suppress("UNCHECKED_CAST")
                    repo.encode(propertyName, newValue as Set<String>)
                }
                isAssignableFrom<Serializable>() -> repo.encode(propertyName, serializeObj(newValue))
                else -> throwNotSupportDataType(dataType)
            }
        }
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
