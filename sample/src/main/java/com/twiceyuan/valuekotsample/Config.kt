package com.twiceyuan.valuekotsample

import android.util.Log
import com.twiceyuan.valuekit.*

/**
 * Created by twiceYuan on 2017/12/8.
 *
 * 项目持久化配置
 */
@ValueKitItem(name = "config")
object Config {

    private const val TAG = "Config"

    // 计数器 1
    var counter1 by intValue()

    // 计数器 2
    var counter2 by intValue(writeValueInterceptor = {
        Log.i(TAG, "write launch count: $it");it
    })

    // 用户名
    var username by stringValue(readInterceptor = { "Mr. $it" })

    // 存储一个个人信息
    var person by objectValue<Person>()
}
