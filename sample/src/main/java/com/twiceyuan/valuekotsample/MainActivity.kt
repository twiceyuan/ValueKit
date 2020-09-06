package com.twiceyuan.valuekotsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tencent.mmkv.MMKV
import com.twiceyuan.valuekit.ValueKitLogger
import com.twiceyuan.valuekit.ext.mmkv.MmkvRegistry
import com.twiceyuan.valuekit.registry.SharedPreferencesRegistry
import com.twiceyuan.valuekit.setupValueKitDefaultRegistry
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ValueKitLogger.isEnable = true
        MMKV.initialize(applicationContext)
        setupValueKitDefaultRegistry(MmkvRegistry())

        setContentView(R.layout.activity_main)

        initViewContent()

        PersonInfo.username = "twiceYuan"
        PersonInfo.counter1 = (PersonInfo.counter1 ?: 0) + 1
        PersonInfo.counter2 = (PersonInfo.counter2 ?: 0) + 1

        PersonInfo.person = PersonInfo.person?.apply {
            accessTime.add(System.currentTimeMillis())
        } ?: Person(name = "用户昵称", email = "somebody@example.com")
    }

    private fun initViewContent() {
        tv_name.text = PersonInfo.username ?: getString(R.string.empty)
        tv_launch_count.text = PersonInfo.counter1.toString()
        tv_person.text = PersonInfo.person?.display() ?: getString(R.string.empty)
    }
}
