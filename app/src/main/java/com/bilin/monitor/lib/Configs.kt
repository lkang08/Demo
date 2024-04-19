package com.bilin.monitor.lib

import android.os.Handler
import android.os.Looper
import android.util.Log

object Configs {

    var maxLimitBitmapSize = 1024 * 1024 * 1

    //超过限制是否直接崩溃
    var crashLimit = true

    var TRY_CRASH = 6

    //crashLimit = true 下，如果图片超出20M这个限制直接崩溃，引起重视必须修复
    var crashLimitBitmapSize = 1024 * 1024 * 20

    private val handle by lazy {
        Handler(Looper.getMainLooper()) {
            when (it.what) {
                TRY_CRASH -> {
                    Log.i("BitmapMonitor", "TRY_CRASH :${it.obj}")
                    throw  Throwable("too large Bitmap try crash :${it.obj}")
                }
                else -> {}
            }
            false
        }
    }

    fun tryCash(str: StringBuilder) {
        handle.obtainMessage(TRY_CRASH, 0, 0, str).sendToTarget()
    }
}