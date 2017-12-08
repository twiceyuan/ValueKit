package com.twiceyuan.valuekotsample

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
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

        Config.person?.let {
            tv_person.text = "name: ${it.name}\nemail: ${it.email}\naccess count: ${it.accessTime.size}"
            it.accessTime.add(System.currentTimeMillis())

            Config.person = it
        }

        if (Config.person == null) {
            Config.person = Person(
                    name = "Somebody",
                    email = "somebody@example.com"
            )
        }
    }
}
