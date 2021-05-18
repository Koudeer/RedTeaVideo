package com.koudeer.lib.widget

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.koudeer.lib.enum.Status
import com.koudeer.lib.enum.type

fun RedTeaVideo.videoGesture(context: Context): GestureDetector =
    GestureDetector(context.applicationContext, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            Log.d(TAG, "onDoubleTap: ${mStatus.type}")
            when (mStatus) {
                Status.PLAYING -> {
                    mMedia.pause()
                    mStatus = Status.PAUSE
                }
                Status.PAUSE -> {
                    mMedia.start()
                    mStatus = Status.PLAYING
                }
            }
            return super.onDoubleTap(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            if (mStatus == Status.NORMAL) {
                mImgStart.performClick()
            }
            return super.onSingleTapConfirmed(e)
        }
    })
