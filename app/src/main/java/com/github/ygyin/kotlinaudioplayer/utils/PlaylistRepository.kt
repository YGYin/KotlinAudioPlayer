package com.github.ygyin.kotlinaudioplayer.utils

import android.provider.MediaStore
import com.github.ygyin.kotlinaudioplayer.data.Music

interface PlaylistRepository {

    suspend fun getMusic(): List<Music>
}