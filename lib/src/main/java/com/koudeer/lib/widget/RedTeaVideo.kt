package com.koudeer.lib.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.*
import com.koudeer.lib.R
import com.koudeer.lib.enum.Status
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.lang.RuntimeException
import java.util.*

class RedTeaVideo : FrameLayout, IVideo, View.OnClickListener, View.OnTouchListener {
    val TAG = "RedTeaVideo"

    private var mUrl: String = ""
    var mStatus = Status.NORMAL
    private var mGesture: GestureDetector
    internal var mMedia: IRedTeaMediaPlayer? = null

    private lateinit var mTexture: TextureView

    lateinit var mTextureContainer: FrameLayout
    lateinit var mBottomContainer: RelativeLayout
    lateinit var mImgStart: ImageView
    lateinit var mProgress: ProgressBar
    lateinit var mSeekBar: SeekBar
    lateinit var mTvTime: TextView
    lateinit var mImgFullNormal: ImageView //全屏普通屏切换
    lateinit var mBottomController: ImageView

    internal var mBottomTimerTask: TimerTask? = null
    internal var mBottomTimer: Timer? = null

    private var ON_PAUSE_PLAYING = -1 //按下Home键时是以PAUSE状态还是PLAYING状态

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
        initEvent()

        mGesture = createVideoGesture(context)
    }

    private fun initView() {
        mTextureContainer = findViewById(R.id.texture_container)
        mImgStart = findViewById(R.id.img_center_controller)
        mProgress = findViewById(R.id.progress)

        mBottomContainer = findViewById(R.id.bottom_container)
        mSeekBar = findViewById(R.id.progress_seekbar)
        mTvTime = findViewById(R.id.tv_progress_time)
        mImgFullNormal = findViewById(R.id.img_screen_full_normal)
        mBottomController = findViewById(R.id.img_bottom_controller)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEvent() {
        mImgStart.setOnClickListener(this)
        mTextureContainer.setOnTouchListener(this)
        mTextureContainer.isClickable = true
    }

    fun setUrl(url: String): Unit {
        if (TextUtils.isEmpty(url)) throw IllegalArgumentException("Url 为空")
        mUrl = url
    }

    fun startVideo(url: String): Unit {
        setUrl(url)
        startVideo()
    }

    fun startVideo(): Unit {
        if (TextUtils.isEmpty(mUrl)) throw RuntimeException("Url 为空")
        addTextureView()
    }

    fun onPauseLifeCycle(): Unit {
        when (mStatus) {
            Status.PAUSE -> {
                ON_PAUSE_PLAYING = mStatus
                mMedia?.pause()
            }
            Status.PLAYING -> {
                ON_PAUSE_PLAYING = mStatus
                mMedia?.pause()
            }
        }
    }

    fun onResumeLifeCycle(): Unit {
        if (ON_PAUSE_PLAYING == Status.PLAYING) {
            mMedia?.start()
        } else if (ON_PAUSE_PLAYING == Status.PAUSE) {
            mMedia?.pause()
        }
        ON_PAUSE_PLAYING = -1
    }

    private fun addTextureView() {
        mTexture = TextureView(context)
        mMedia = RedTeaMediaPlayer(this)
        mTexture.surfaceTextureListener = mMedia
        mTextureContainer.addView(mTexture)
    }

    private fun createBottomTimerTask() {

    }

    private fun cancelBottomTimerTask() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_center_controller -> startVideo()
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (v?.id == R.id.texture_container) {
            mGesture.onTouchEvent(event)
        }
        return false
    }

    //ijkplayer prepare回调
    override fun prepare() {
        mStatus = Status.PREPARE
        mProgress.visibility = VISIBLE
        mImgStart.visibility = GONE
        mMedia?.start()
    }

    override fun onInfo(what: Int, extra: Int) {
        //IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START 渲染视频的第一帧 说明进入播放状态
        Log.d(TAG, "onInfo: ")
        if (what == IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            Log.d(TAG, "onInfo: PLAYING ${Thread.currentThread().name}")
            mStatus = Status.PLAYING
            mProgress.visibility = GONE
        }
    }

    override fun onSurfaceTexture(surfaceTexture: SurfaceTexture) {
        mTexture.setSurfaceTexture(surfaceTexture)
    }

    override fun onError() {

    }

    override fun onCompletion() {

    }

    override fun getUrl(): String = mUrl

    /**
     * 所有任务定时器
     */
    internal class AllTimerTask(private val fn: () -> Unit) : TimerTask() {
        override fun run() {
            fn.invoke()
        }
    }
}