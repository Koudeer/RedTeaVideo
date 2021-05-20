package com.koudeer.lib.widget

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.koudeer.lib.enum.Status
import com.koudeer.lib.enum.type
import java.util.*

/**
 * 创建手势
 */
internal fun RedTeaVideo.createVideoGesture(context: Context): GestureDetector =
    GestureDetector(context.applicationContext, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            Log.d(TAG, "onDoubleTap: ${mStatus.type}")
            when (mStatus) {
                Status.PLAYING -> {
                    mMedia?.pause()
                    mStatus = Status.PAUSE
                }
                Status.PAUSE -> {
                    mMedia?.start()
                    mStatus = Status.PLAYING
                }
            }
            //双击都会显示底部控制器容器
            mBottomContainer.visibility = View.VISIBLE
            createBottomContainerTask()
            return super.onDoubleTap(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            when (mStatus) {
                Status.NORMAL -> {
                    mImgStart.performClick()
                }
                Status.PLAYING -> {
                    visiblityBottomContainerSet()
                }
            }
            return super.onSingleTapConfirmed(e)
        }
    })

/**
 * 定时任务更新进度条
 */
internal fun RedTeaVideo.updateProgress() {
    post {
        val p = mMedia!!.getCurrentPosition()
        val d = mMedia!!.getDuration()
        val progress = (p * 100 / if (d == 0L) 1 else d).toInt()
        mSeekBar.progress = progress

        if (p != 0L) mTvTime.text = stringForTime(p)

        //播放时 有时候Progress不会消失，这里可能会影响一些性能
        if (mProgress.visibility == View.VISIBLE) mProgress.visibility = View.INVISIBLE
    }
}

/**
 * 创建底部容器定时设置可见性为invisible
 */
internal fun RedTeaVideo.createBottomContainerTask() {
    cancelBottomContainerTask()
    mBottomTimerTask = RedTeaVideo.AllTimerTask {
        post {
            mBottomContainer.visibility = View.INVISIBLE
        }
    }
    mBottomTimer = Timer()
    //底部容器3秒后隐藏
    mBottomTimer?.schedule(mBottomTimerTask, 3000)
}

/**
 * 取消底部容器定时器
 */
internal fun RedTeaVideo.cancelBottomContainerTask() {
    mBottomTimer?.cancel()
    mBottomTimerTask?.cancel()
}

/**
 * 底部容器可见性设置
 */
internal fun RedTeaVideo.visiblityBottomContainerSet() {
    if (mBottomContainer.visibility == View.VISIBLE) {
        mBottomContainer.visibility = View.INVISIBLE
        //cancel
        cancelBottomContainerTask()
    } else if (mBottomContainer.visibility == View.INVISIBLE) {
        mBottomContainer.visibility = View.VISIBLE
        //create
        createBottomContainerTask()
    }
}


internal fun RedTeaVideo.createProgressTimerTask() {
    cancelProgressTimerTask()
    mProgressTimerTask = RedTeaVideo.AllTimerTask {
        updateProgress()
    }
    mProgressTimer = Timer()
    mProgressTimer?.schedule(mProgressTimerTask, 0, 500)
}

internal fun RedTeaVideo.cancelProgressTimerTask() {
    mProgressTimerTask?.cancel()
    mProgressTimer?.cancel()
}

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
