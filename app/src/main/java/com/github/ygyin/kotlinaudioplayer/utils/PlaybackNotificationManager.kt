package com.github.ygyin.kotlinaudioplayer.utils

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.github.ygyin.kotlinaudioplayer.R
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.NotificationUtil.IMPORTANCE_DEFAULT
import com.google.android.exoplayer2.util.NotificationUtil.createNotificationChannel
import kotlinx.coroutines.*

const val CHANNEL_ID ="com.github.ygyin.kotlinaudioplayer.NOW_PLAYING"
const val NOTIFICATION_ID =0x123

class PlaybackNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener
) : CoroutineScope by MainScope() {
    // TODO: FOREGROUND NOTIFICATION 
}

