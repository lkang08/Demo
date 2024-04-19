package com.example.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var v = findViewById<ImageView>(R.id.iv4)
        var v2 = findViewById<ImageView>(R.id.iv)
        Log.d("Test", "${v.javaClass} ${v.javaClass.superclass}")
        Log.d("Test", "${v2.javaClass} ${v2.javaClass.superclass}")
    }
}