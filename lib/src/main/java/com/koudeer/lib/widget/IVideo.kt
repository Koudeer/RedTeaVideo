package com.koudeer.lib.widget

import android.graphics.SurfaceTexture

interface IVideo {

    fun prepare()

    fun onInfo(what: Int, extra: Int)

    fun onSurfaceTexture(surfaceTexture: SurfaceTexture)

    fun getUrl(): String

    fun onError()

    fun onCompletion()

    fun onBuffer(buf: Int)

    fun onVideoSizeChange(width: Int?, height: Int?)
}