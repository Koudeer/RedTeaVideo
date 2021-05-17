package com.koudeer.redteavideo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.koudeer.lib.widget.RedTeaVideo

class MainActivity : AppCompatActivity() {

    private lateinit var mVideo: RedTeaVideo
    private val url =
        "https://vd2.bdstatic.com/mda-igwdmz0nv8ah95xv/mda-igwdmz0nv8ah95xv.mp4?playlist=%5B%22hd%22%2C%22sc%22%5D&auth_key=1567068990-0-0-003a15194906c2b947cf493eec963a7f&bcevod_channel=searchbox_feed&pd=bjh"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mVideo = findViewById(R.id.video)

        mVideo.startVideo(url)
    }
}