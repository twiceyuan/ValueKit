package com.twiceyuan.valuekotsample

import java.io.Serializable

/**
 * Created by twiceYuan on 2017/12/8.
 *
 * 数据模型类
 */
data class Person(
        val name: String,
        val email: String,
        val accessTime: MutableList<Long> = ArrayList()
) : Serializable