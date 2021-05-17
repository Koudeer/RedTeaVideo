package com.koudeer.lib.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.TextureView
import android.widget.FrameLayout
import com.koudeer.lib.R

class RedTeaVideo : FrameLayout, IVideo {

    private var mUrl: String = ""
    private lateinit var mMedia: IRedTeaMediaPlayer

    private lateinit var mTexture: TextureView

    private lateinit var mTextureContainer: FrameLayout

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        inflate(context, R.layout.layout_video, this)
        initView()
    }

    private fun initView(): Unit {
        mTextureContainer = findViewById(R.id.texture_container)
    }

    fun startVideo(url: String): Unit {
        mUrl = url
        addTextureView()
    }

    private fun addTextureView() {
        mTexture = TextureView(context)
        mMedia = RedTeaMediaPlayer(this)
        mTexture.surfaceTextureListener = mMedia
        mTextureContainer.addView(mTexture)
    }

    override fun prepare() {
        mMedia.start()
    }

    override fun onInfo(what: Int, extra: Int) {
    }

    override fun onSurfaceTexture(surfaceTexture: SurfaceTexture) {
    }

    override fun getUrl(): String = mUrl
}