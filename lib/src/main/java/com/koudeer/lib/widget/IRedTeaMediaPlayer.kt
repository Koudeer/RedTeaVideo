package com.koudeer.lib.widget

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.view.TextureView

abstract class IRedTeaMediaPlayer(val iVideo: IVideo) : TextureView.SurfaceTextureListener {

    var mSurfaceTexture: SurfaceTexture? = null
    lateinit var mHandler: Handler
    lateinit var mMediaHandler: Handler
    lateinit var mHandlerThread: HandlerThread

    abstract fun prepare()

    abstract fun start()

    abstract fun pause()

    abstract fun isPlayer(): Boolean

    abstract fun getCurrentPosition(): Long

    abstract fun getDuration(): Long

    abstract fun seekTo(p: Long);

}