package com.koudeer.lib.widget

import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Surface
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

class RedTeaMediaPlayer(iVideo: IVideo) : IRedTeaMediaPlayer(iVideo),
    IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener,
    IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener,
    IMediaPlayer.OnBufferingUpdateListener {
    private val TAG = "RedTeaMediaPlayer"

    private lateinit var mMedia: IjkMediaPlayer

    override fun prepare() {
        mHandler = Handler(Looper.getMainLooper())
        mHandlerThread = HandlerThread("IJK")
        mHandlerThread.start()
        mMediaHandler = Handler(mHandlerThread.looper)

        mMediaHandler.post {
            mMedia = IjkMediaPlayer()

            //自动播放 0关闭 1开启
            mMedia.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)
            //设置seekTo能够快速seek到指定位置并播放
            mMedia.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "fflags", "fastseek")
            mMedia.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 10)
            //1硬解 0软解
            mMedia.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "mediacodec", 0)
            //某些视频在SeekTo的时候，会跳回到拖动前的位置，这是因为视频的关键帧的问题，通俗一点就是FFMPEG不兼容，视频压缩过于厉害，seek只支持关键帧，出现这个情况就是原始的视频文件中i 帧比较少
            mMedia.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "enable-accurate-seek", 1)

            mMedia.setOnPreparedListener(this)
            mMedia.setOnInfoListener(this)
            mMedia.setOnCompletionListener(this)
            mMedia.setOnErrorListener(this)
            mMedia.setOnBufferingUpdateListener(this)

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
        } else {
            iVideo.onSurfaceTexture(mSurfaceTexture!!)
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun onPrepared(p0: IMediaPlayer?) {
        mHandler.post { iVideo.prepare() }
    }

    override fun onInfo(p0: IMediaPlayer?, p1: Int, p2: Int): Boolean {
        mHandler.post { iVideo.onInfo(p1, p2) }
        return false
    }

    override fun onCompletion(p0: IMediaPlayer?) {
        mHandler.post { iVideo.onCompletion() }
    }

    override fun onError(p0: IMediaPlayer?, p1: Int, p2: Int): Boolean {
        mHandler.post { iVideo.onError() }
        return false
    }

    override fun getCurrentPosition(): Long {
        return mMedia.currentPosition
    }

    override fun getDuration(): Long {
        return mMedia.duration
    }

    override fun onBufferingUpdate(p0: IMediaPlayer?, p1: Int) {
        mHandler.post {
            iVideo.onBuffer(p1)
        }
    }

    override fun seekTo(p: Long) {
        mMediaHandler.post {
            try {
                mMedia.seekTo(p)
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }
}