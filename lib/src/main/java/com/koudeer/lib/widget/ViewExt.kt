package com.koudeer.lib.widget

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.koudeer.lib.R
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
            controllerToggleUI()
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
                Status.PLAYING, Status.PAUSE -> {
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

        if (p != 0L) mTvTime.text = String.format("%s/%s",stringForTime(d),stringForTime(p))

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

/**
 * 进度条定时器
 */
internal fun RedTeaVideo.createProgressTimerTask() {
    cancelProgressTimerTask()
    mProgressTimerTask = RedTeaVideo.AllTimerTask {
        updateProgress()
    }
    mProgressTimer = Timer()
    mProgressTimer?.schedule(mProgressTimerTask, 0, 500)
}

/**
 * 取消进度条定时器
 */
internal fun RedTeaVideo.cancelProgressTimerTask() {
    mProgressTimerTask?.cancel()
    mProgressTimer?.cancel()
}

/**
 * 底部控制器 管理事件 UI切换
 */
internal fun RedTeaVideo.controllerToggleUI() {
    when (mStatus) {
        Status.PLAYING -> {
            mMedia?.pause()
            mStatus = Status.PAUSE
            mBottomController.setImageResource(R.mipmap.start_mini)
        }
        Status.PAUSE -> {
            mMedia?.start()
            mStatus = Status.PLAYING
            mBottomController.setImageResource(R.mipmap.pause_mini)
        }
    }
}

internal fun RedTeaVideo.openFullScreen() {

}
