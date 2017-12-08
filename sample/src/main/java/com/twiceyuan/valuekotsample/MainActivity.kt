package com.twiceyuan.valuekotsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Config.username?.let {
            tv_name.text = it
        }

        Config.username = "twiceYuan"

        if (Config.launchCount == null) {
            Config.launchCount = 1
        }

        Config.launchCount?.let {
            tv_launch_count.text = "$it"
            Config.launchCount = it + 1
        }
    }
}
