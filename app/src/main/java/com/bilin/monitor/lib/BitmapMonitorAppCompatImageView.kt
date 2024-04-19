package com.bilin.monitor.lib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Looper
import android.os.MessageQueue
import android.util.AttributeSet

open class BitmapMonitorAppCompatImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr), MessageQueue.IdleHandler {

    companion object {
        const val TAG = "BitmapMonitorAppCompat"
    }

    private var traceBitmapMethod: String? = null
    private var traceWith: Int = 0
    private var traceHeight: Int = 0
    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        monitor("setImageDrawable")
    }

    override fun setBackground(background: Drawable?) {
        super.setBackground(background)
        monitor("setBackground")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.traceHeight = h
        this.traceWith = w
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        monitor("setImageBitmap")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Looper.myQueue().removeIdleHandler(this)
    }


    private fun monitor(methodName: String) {
        this.traceBitmapMethod = methodName
        Looper.myQueue().removeIdleHandler(this)
        Looper.myQueue().addIdleHandler(this)
    }

    override fun queueIdle(): Boolean {
        this.printBitmapInfo(this.traceBitmapMethod, traceHeight, traceWith, TAG)
        return false
    }
}