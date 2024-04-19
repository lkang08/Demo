package com.example.demo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter

@Route(path = "/app/test/main1")
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var v = findViewById<ImageView>(R.id.iv4)
        var v2 = findViewById<ImageView>(R.id.iv)
        Log.d("Test", "${v.javaClass} ${v.javaClass.superclass}")
        Log.d("Test", "${v2.javaClass} ${v2.javaClass.superclass}")
        v2.setOnClickListener {
            ARouter.getInstance().build("/home/test/homeActivity")
                .navigation()
        }
        BuildConfig.FLAVOR
        v.setOnClickListener {
            ARouter.getInstance().build("/app/test/main2").navigation()
        }
        findViewById<ImageView>(R.id.iv2).setOnClickListener {
            ARouter.getInstance().build("/mylibrary/test").navigation()
        }
    }
}