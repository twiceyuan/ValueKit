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
        Config.launchCount = Config.launchCount ?: 0 + 1

        val person = Config.person
        if (person != null) {
            person.accessTime.add(System.currentTimeMillis())
            Config.person = person
        } else {
            Config.person = Person(name = "Somebody", email = "somebody@example.com")
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initViewContent() {
        tv_name.text = Config.username ?: "NULL"
        tv_launch_count.text = Config.launchCount.toString()
        tv_person.text = """|name: ${Config.person?.name}
                            |email: ${Config.person?.email}
                            |access count: ${Config.person?.accessTime?.size}""".trimMargin()
    }
}
