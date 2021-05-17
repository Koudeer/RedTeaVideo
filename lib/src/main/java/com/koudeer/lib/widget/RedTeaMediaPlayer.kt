package com.koudeer.lib.widget

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Surface
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class RedTeaMediaPlayer(iVideo: IVideo) : IRedTeaMediaPlayer(iVideo) {

    private lateinit var mMedia: IMediaPlayer

    override fun prepare() {
        mHandler = Handler(Looper.getMainLooper())
        mHandlerThread = HandlerThread("IJK")
        mHandlerThread.start()
        mMediaHandler = Handler(mHandlerThread.looper)

        mMediaHandler.post {
            mMedia = IjkMediaPlayer()

            mMedia.setDataSource(iVideo.getUrl())
            mMedia.prepareAsync()
            mMedia.setSurface(Surface(mSurfaceTexture))
        }
    }

    override fun start() {
        mMediaHandler.post { mMedia.start() }
    }

    override fun pause() {
        mMediaHandler.post { mMedia.pause() }
    }

    override fun isPlayer(): Boolean {
        return mMedia.isPlaying
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (mSurfaceTexture == null) {
            mSurfaceTexture = surface
            prepare()
        }else{
            iVideo.onSurfaceTexture(surface)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }
}