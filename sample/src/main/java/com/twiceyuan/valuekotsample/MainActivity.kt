package com.twiceyuan.valuekotsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.twiceyuan.valuekit.DefaultRegistry
import com.twiceyuan.valuekit.setupValueKitDefaultRegister
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupValueKitDefaultRegister(DefaultRegistry(applicationContext))

        setContentView(R.layout.activity_main)

        initViewContent()

        Config.username = "twiceYuan"
        Config.counter1 = (Config.counter1 ?: 0) + 1
        Config.counter2 = (Config.counter2 ?: 0) + 1

        Config.person = Config.person?.apply {
            accessTime.add(System.currentTimeMillis())
        } ?: Person(name = "用户昵称", email = "somebody@example.com")
    }

    private fun initViewContent() {
        tv_name.text = Config.username ?: getString(R.string.empty)
        tv_launch_count.text = Config.counter1.toString()
        tv_person.text = Config.person.toString()
    }
}
