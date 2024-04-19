package com.bilin.monitor.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Looper
import android.os.MessageQueue
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bilin.monitor.lib.Configs.crashLimit
import com.bilin.monitor.lib.Configs.crashLimitBitmapSize
import com.bilin.monitor.lib.Configs.maxLimitBitmapSize

@SuppressLint("AppCompatCustomView")
open class BitmapMonitorImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : android.widget.ImageView(context, attrs, defStyleAttr), MessageQueue.IdleHandler {

    companion object {
        const val TAG = "BitmapMonitorImageView"
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


fun ImageView.printBitmapInfo(methodName: String?, h: Int = 0, w: Int = 0, tag: String = "BitmapMonitor") {
    val str = StringBuilder()
    val start = System.currentTimeMillis()
    val mDrawable = (if (methodName == "setBackground") this.background else this.drawable) ?: return
    val viewWidth = if (w > 0) w else mDrawable.intrinsicWidth
    val viewHeight = if (h > 0) h else mDrawable.intrinsicHeight
    val bitmap = mDrawable.getDrawableBitmap() ?: return
    val drawableWidth = bitmap.width
    val drawableHeight = bitmap.height
    val imageSize = bitmap.allocationByteCount
    if (imageSize <= 0) {
        return
    }
    val resName = kotlin.runCatching { this.resources.getResourceEntryName(this.id) }.getOrNull() ?: this.id
    str.append("$methodName")
    if (imageSize > maxLimitBitmapSize) {
        str.append(" too large")
    }
    str.append(" ${imageSize / 1024L}Kb,${imageSize}B")
    str.append(" $resName")
    str.append(" ${this.javaClass.simpleName}")
    str.append(" ${this.context.findActivity()?.javaClass?.simpleName}")
    if (drawableWidth > viewWidth || drawableHeight > viewHeight) {
        str.append(" over size")
    }
    str.append(" bitmapW:$drawableWidth bitmapH:$drawableHeight,viewW:$viewWidth viewH:$viewHeight")

    str.append(" visible:${this.visibility == View.VISIBLE}")
    str.append(" take:${System.currentTimeMillis() - start}")

    if (imageSize < (1024 * 1024 * 1)) {
        Log.i(tag, "$str")
    } else if (imageSize >= (1024 * 1024 * 1) && imageSize < (1024 * 1024 * 5)) {
        Log.d(tag, "$str")
    } else if (imageSize >= (1024 * 1024 * 5) && imageSize < (1024 * 1024 * 15)) {
        Log.w(tag, "$str")
    } else {
        Log.e(tag, "$str")
    }
    if (crashLimit && imageSize > crashLimitBitmapSize) {
        Configs.tryCash(str)
    }
}


@SuppressLint("SoonBlockedPrivateApi")
fun Drawable?.getDrawableBitmap(): Bitmap? {
    this ?: return null
    return when {
        this is BitmapDrawable -> {
            this.bitmap
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && this is AdaptiveIconDrawable -> {
            kotlin.runCatching {
                AdaptiveIconDrawable::class.java.getDeclaredField("mLayersBitmap").let {
                    it.isAccessible = true
                    it.get(this) as? Bitmap
                }
            }.getOrNull()
        }
        else -> {
            //检查属性里面有没有bitmap ,有从里拿一个
            kotlin.runCatching {
                val fields = (this::class.java).declaredFields ?: return null
                val bitmapFiled = fields?.firstOrNull {
                    Bitmap::class.java.isAssignableFrom(it.type)
                } ?: return null
                bitmapFiled.isAccessible = true
                bitmapFiled.get(this) as? Bitmap
            }.onFailure {
                Log.e(BitmapMonitorImageView.TAG, "getBitmap err:${this::class.java.name},$it")
            }.getOrNull()
        }
    }
}

private fun Context?.findActivity(): Activity? {
    return when (this) {
        is Activity -> {
            this
        }
        is ContextWrapper -> {
            this.baseContext.findActivity()
        }
        else -> {
            null
        }
    }
}