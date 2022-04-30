package com.github.ygyin.kotlinaudioplayer.ui.nowplaying

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.ygyin.kotlinaudioplayer.R

class NowPlayingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_now_playing)
        supportActionBar?.hide()
    }
}