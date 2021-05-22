package com.koudeer.redteavideo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.koudeer.lib.widget.RedTeaVideo

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var mVideo: RedTeaVideo
    private val url =
        "https://vd2.bdstatic.com/mda-igwdmz0nv8ah95xv/mda-igwdmz0nv8ah95xv.mp4?playlist=%5B%22hd%22%2C%22sc%22%5D&auth_key=1567068990-0-0-003a15194906c2b947cf493eec963a7f&bcevod_channel=searchbox_feed&pd=bjh"

    private val rtmp = "rtmp://58.200.131.2:1935/livetv/hunantv"

    private val rtsp = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mVideo = findViewById(R.id.video)
        mVideo.setUrl(url)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        mVideo.onResumeLifeCycle()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        mVideo.onPauseLifeCycle()
    }
}