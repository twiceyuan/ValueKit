package com.twiceyuan.valuekotsample

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewContent()

        Config.username = "twiceYuan"
        Config.launchCount = (Config.launchCount ?: 0) + 1

        Config.person = Config.person?.apply {
            accessTime.add(System.currentTimeMillis())
        } ?: Person(name = "用户昵称", email = "somebody@example.com")
    }

    private fun initViewContent() {
        tv_name.text = Config.username ?: getString(R.string.empty)
        tv_launch_count.text = Config.launchCount.toString()
        tv_person.text = Config.person.toString()
    }
}
