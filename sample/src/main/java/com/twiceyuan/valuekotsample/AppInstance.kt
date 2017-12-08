package com.twiceyuan.valuekotsample

import android.app.Application
import com.twiceyuan.valuekit.ValueKit

/**
 * Created by twiceYuan on 2017/12/8.
 *
 * App Instance
 */
class AppInstance : Application() {
    override fun onCreate() {
        super.onCreate()
        ValueKit.init(this)
    }
}