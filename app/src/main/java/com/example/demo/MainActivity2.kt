package com.example.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter

@Route(path = "/app/test/main2")
class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        var v = findViewById<ImageView>(R.id.iv2)
        var v2 = findViewById<ImageView>(R.id.iv)
        Log.d("Test22", "${v.javaClass} ${v.javaClass.superclass}")
        Log.d("Test22", "${v2.javaClass} ${v2.javaClass.superclass}")
        v2.setOnClickListener {
            ARouter.getInstance().build("/app/test/main1").navigation()
        }
    }
}