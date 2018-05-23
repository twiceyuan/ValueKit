package com.twiceyuan.valuekit

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.reflect.KProperty

/**
 * Created by twiceYuan on 2017/12/6.
 *
 * Config
 */
private const val TAG = "ValueKit"
private const val DEFAULT_DIR_NAME = "value_kit"


object IntegerValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): Int? = read(t, property.name)

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: Int?) {
        write(t, property.name, newValue)
    }
}

object BooleanValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): Boolean? = read(t, property.name)

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: Boolean?) {
        write(t, property.name, newValue)
    }
}

object LongValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): Long? = read(t, property.name)

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: Long?) {
        write(t, property.name, newValue)
    }
}

object DoubleValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): Double? = read(t, property.name)

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: Double?) {
        write(t, property.name, newValue)
    }
}

object StringValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): String? = read(t, property.name)

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: String?) {
        write(t, property.name, newValue)
    }
}

class ObjectValue<Object : Serializable> {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): Object? = read(t, property.name)

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: Object?) {
        write(t, property.name, newValue)
    }
}

fun <T : Any, Data : Any> read(any: T, propertyName: String): Data? {

    val valueDirAnnotation = any.javaClass.getAnnotation(ValueDir::class.java)

    File(ValueKit.valueDir(valueDirAnnotation?.value), propertyName).apply {
        return if (exists())
            return try {
                @Suppress("UNCHECKED_CAST")
                ObjectInputStream(FileInputStream(this)).readObject() as Data
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                null
            }
        else
            null
    }
    return null
}

fun <T : Any> write(any: T, propertyName: String, newValue: Any?) {

    val valueDirAnnotation = any.javaClass.getAnnotation(ValueDir::class.java)

    File(ValueKit.valueDir(valueDirAnnotation?.value), propertyName).apply {
        createNewFile()
        ObjectOutputStream(FileOutputStream(this)).writeObject(newValue)
    }
}

object ValueKit {

    private lateinit var fileDir: File

    fun init(context: Context) {
        fileDir = context.filesDir
    }

    fun init(file: File) {
        fileDir = file
    }

    fun valueDir(dirName: String? = null): File = File(fileDir, dirName ?: DEFAULT_DIR_NAME).apply { mkdirs() }
}

annotation class ValueDir(val value: String)
