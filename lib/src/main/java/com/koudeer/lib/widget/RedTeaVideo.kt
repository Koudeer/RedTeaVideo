package com.koudeer.lib.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import com.koudeer.lib.R
import com.koudeer.lib.enum.Screen
import com.koudeer.lib.enum.Status
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.*

class RedTeaVideo : FrameLayout, IVideo, View.OnClickListener, View.OnTouchListener,
    SeekBar.OnSeekBarChangeListener {
    val TAG = "RedTeaVideo"

    private var mUrl: String = ""
    var mStatus = Status.NORMAL
    var mScreen = Screen.NORMAL_SCREEN
    private var mGesture: GestureDetector
    internal var mMedia: IRedTeaMediaPlayer? = null

    internal var mLayoutParams: ViewGroup.LayoutParams? = null
    internal var mVideoIndex = -1
    internal var mParentViewGroup: ViewGroup? = null

    private lateinit var mTexture: TextureView

    lateinit var mTextureContainer: FrameLayout
    lateinit var mBottomContainer: RelativeLayout
    lateinit var mImgStart: ImageView
    lateinit var mProgress: ProgressBar
    lateinit var mSeekBar: SeekBar
    lateinit var mTvTime: TextView
    lateinit var mImgFullNormal: ImageView //全屏普通屏切换
    lateinit var mBottomController: ImageView

    //seekbar 拖动flag
    private var isSeekTouch = false

    //底部容器定时器
    internal var mBottomTimerTask: TimerTask? = null
    internal var mBottomTimer: Timer? = null

    //进度定时器
    internal var mProgressTimerTask: TimerTask? = null
    internal var mProgressTimer: Timer? = null

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
        mSeekBar.setOnSeekBarChangeListener(this)
        mBottomController.setOnClickListener(this)
        mImgFullNormal.setOnClickListener(this)
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
        cancelProgressTimerTask()
        cancelBottomContainerTask()
    }

    fun onResumeLifeCycle(): Unit {
        if (ON_PAUSE_PLAYING == Status.PLAYING) {
            mMedia?.start()
        } else if (ON_PAUSE_PLAYING == Status.PAUSE) {
            mMedia?.pause()
        }
        //防止取消后不会更新progress
        if (ON_PAUSE_PLAYING != -1) {
            createProgressTimerTask()
            createBottomContainerTask()
        }
        ON_PAUSE_PLAYING = -1
    }

    private fun addTextureView() {
        mTexture = TextureView(context)
        mMedia = RedTeaMediaPlayer(this)
        mTexture.surfaceTextureListener = mMedia
        mTextureContainer.addView(mTexture)

        mProgress.visibility = VISIBLE
        mImgStart.visibility = GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.img_center_controller -> startVideo()
            R.id.img_bottom_controller -> {
                //这里改变底部暂停键UI
                controllerToggleUI()
            }
            R.id.img_screen_full_normal -> {
                //需要在文件清单添加 android:configChanges="orientation|screenSize" 这样就不会触发Pause Resume生命周期
                when (mScreen) {
                    Screen.NORMAL_SCREEN -> openFullScreen()
                    Screen.FULL_SCREEN -> closeFullScreen()
                }
            }
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
        Log.d(TAG, "prepare: ")
        mStatus = Status.PREPARE
        mMedia?.start()
    }

    override fun onInfo(what: Int, extra: Int) {
        //IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START 渲染视频的第一帧 说明进入播放状态
        if (what == IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
            Log.d(TAG, "onInfo: PLAYING ${Thread.currentThread().name}")
            mStatus = Status.PLAYING
            mProgress.visibility = INVISIBLE
            createProgressTimerTask()
        }
        if (what == IjkMediaPlayer.MEDIA_INFO_BUFFERING_START) {
            Log.d(TAG, "onInfo 开始缓冲: $what  $extra")
            mProgress.visibility = View.VISIBLE
        }
        if (what == IjkMediaPlayer.MEDIA_INFO_BUFFERING_END) {
            Log.d(TAG, "onInfo 结束缓冲: $what  $extra")
            mProgress.visibility = View.INVISIBLE
        }
    }

    override fun onSurfaceTexture(surfaceTexture: SurfaceTexture) {
        Log.d(TAG, "onSurfaceTexture: ")
        mTexture.setSurfaceTexture(surfaceTexture)
    }

    override fun onError() {
        Log.d(TAG, "onError: ")
        mStatus = Status.ERROR
        mImgStart.visibility = VISIBLE
        mProgress.visibility = INVISIBLE
        mImgStart.setImageResource(R.mipmap.replay)
    }

    override fun onCompletion() {
        Log.d(TAG, "onCompletion: ")
        mStatus = Status.COMPLETE
        mImgStart.visibility = VISIBLE
        mImgStart.setImageResource(R.mipmap.replay)
    }

    override fun onBuffer(buf: Int) {
    }

    override fun getUrl(): String = mUrl

    override fun onVideoSizeChange(width: Int?, height: Int?) {
    }

    //Seek
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        //改变文本时间
        if (fromUser) {
            //fromUser 手动拖动是为true
            mMedia?.let {
                //取消进度条计算，不然会疯狂跳动
                if (!isSeekTouch) {//拖动进度条事件 这里只执行一次
                    cancelProgressTimerTask()
                    cancelBottomContainerTask()
                    isSeekTouch = true
                }
                mMedia?.let {
                    val d = it.getDuration()
                    mTvTime.text = String.format(
                        "%s/%s",
                        stringForTime(mMedia?.getDuration()!!),
                        stringForTime(progress * d / 100)
                    )
                }
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        Log.d(TAG, "onStartTrackingTouch: ")
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val time = seekBar.progress * mMedia?.getDuration()!! / 100
        mMedia?.seekTo(time)
        createProgressTimerTask()
        createBottomContainerTask()
        isSeekTouch = false
    }
    //Seek

    /**
     * 所有任务定时器
     */
    internal class AllTimerTask(private val fn: () -> Unit) : TimerTask() {
        override fun run() {
            fn.invoke()
        }
    }
}