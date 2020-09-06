package com.twiceyuan.valuekit

import android.util.Log

/**
 * 日志统一拦截
 */
object ValueKitLogger {

    private const val DEFAULT_TAG = "ValueKitLogger"

    var isEnable = false

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