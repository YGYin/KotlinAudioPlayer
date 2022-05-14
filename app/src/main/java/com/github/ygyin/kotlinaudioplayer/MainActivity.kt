package com.github.ygyin.kotlinaudioplayer

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.github.ygyin.kotlinaudioplayer.ui.playlist.PlaylistFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // sth change

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)
    }


    override fun onStart() {
        super.onStart()

        if (intent?.categories?.contains("android.shortcut.play.song") == true) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
            val playlistFragment =
                navHostFragment.childFragmentManager.fragments[0] as? PlaylistFragment
        }
    }
}