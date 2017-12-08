package com.twiceyuan.valuekit

import android.content.Context
import java.io.*
import kotlin.reflect.KProperty

/**
 * Created by twiceYuan on 2017/12/6.
 *
 * Config
 */
object IntegerValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): Int? = read(t, property.name) as Int?

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: Int?) {
        write(t, property.name, newValue)
    }
}

object BooleanValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): Boolean? = read(t, property.name) as Boolean?

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: Boolean?) {
        write(t, property.name, newValue)
    }
}

object LongValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): Long? = read(t, property.name) as Long?

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: Long?) {
        write(t, property.name, newValue)
    }
}

object DoubleValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): Double? = read(t, property.name) as Double?

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: Double?) {
        write(t, property.name, newValue)
    }
}

object StringValue {
    operator fun <T : Any> getValue(t: T, property: KProperty<*>): String? = read(t, property.name) as String?

    operator fun <T : Any> setValue(t: T, property: KProperty<*>, newValue: String?) {
        write(t, property.name, newValue)
    }
}

fun <T : Any> read(any: T, propertyName: String): Any? {

    val valueDirAnnotation = any.javaClass.getAnnotation(ValueDir::class.java)

    File(ValueKit.valueDir(valueDirAnnotation?.value), propertyName).apply {
        return if (exists())
            ObjectInputStream(FileInputStream(this)).readObject()
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

    private val defaultDirName = "value_kit"

    fun init(context: Context) {
        fileDir = context.filesDir
    }

    fun valueDir(dirName: String? = null): File = File(fileDir, dirName ?: defaultDirName).apply { mkdirs() }
}

annotation class ValueDir(val value: String)
