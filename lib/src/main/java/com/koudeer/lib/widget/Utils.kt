package com.koudeer.lib.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import java.util.*

/**
 * 计算进度时间
 */
internal fun stringForTime(timeMs: Long): String {
    if (timeMs <= 0 || timeMs >= 24 * 60 * 60 * 1000) {
        return "00:00"
    }

    val totalSeconds = timeMs / 1000
    val seconds = totalSeconds % 60.toInt()
    val minutes = (totalSeconds / 60) % 60
    val hours = (totalSeconds / 3600)
    val stringBuilder = StringBuilder()
    val mFormatter = Formatter(stringBuilder, Locale.getDefault())
    if (hours > 0) {
        return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
    } else {
        return mFormatter.format("%02d:%02d", minutes, seconds).toString()
    }
}


/**
 * Get activity from context object
 *
 * @param context context
 * @return object of Activity or null if it is not Activity
 */
internal fun scanForActivity(context: Context?): Activity? {
    if (context == null) return null
    if (context is Activity) {
        return context
    } else if (context is ContextWrapper) {
        return scanForActivity(context.baseContext)
    }
    return null
}