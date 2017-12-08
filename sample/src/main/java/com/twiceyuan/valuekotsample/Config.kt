package com.twiceyuan.valuekotsample

import com.twiceyuan.valuekit.IntegerValue
import com.twiceyuan.valuekit.ObjectValue
import com.twiceyuan.valuekit.StringValue
import com.twiceyuan.valuekit.ValueDir

/**
 * Created by twiceYuan on 2017/12/8.
 *
 * 项目持久化配置
 */
@ValueDir("config")
object Config {

    // 启动次数
    var launchCount by IntegerValue

    // 用户名
    var username by StringValue

    // 存储一个个人信息
    var person by ObjectValue<Person>()
}
