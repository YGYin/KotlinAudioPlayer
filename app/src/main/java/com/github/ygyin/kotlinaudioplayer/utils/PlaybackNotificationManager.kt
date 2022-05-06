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
//    private val notificationManager: PlayerNotificationManager
//
//    init {
//        val mediaController = MediaControllerCompat(context, sessionToken)
//        notificationManager(
//            context,
//            CHANNEL_ID,
//            NOTIFICATION_ID,
//            DescriptionAdapter(mediaController)
//        )
//    }
//
//    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) :
//        PlayerNotificationManager.MediaDescriptionAdapter {
//
//        var currentIconUri: Uri? = null
//        var currentBitmap: Bitmap? = null
//
//        override fun createCurrentContentIntent(player: Player): PendingIntent? =
//            controller.sessionActivity
//
//        override fun getCurrentContentText(player: Player) =
//            controller.metadata.description.subtitle.toString()
//
//        override fun getCurrentContentTitle(player: Player): CharSequence =
//            controller.metadata.description.title.toString()
//
//        override fun getCurrentLargeIcon(
//            player: Player,
//            callback: PlayerNotificationManager.BitmapCallback
//        ): Bitmap? {
//            val iconUri = controller.metadata.description.iconUri
//            return if (currentIconUri != iconUri || currentBitmap == null) {
//
//                // Cache the bitmap for the current song so that successive calls to
//                // `getCurrentLargeIcon` don't cause the bitmap to be recreated.
//                currentIconUri = iconUri
//                launch {
//                    currentBitmap = iconUri?.let {
//                        resolveUriAsBitmap(it)
//                    } ?: run {
//                        getDefaultBitmap(context)
//                    }
//
//                    currentBitmap?.let { callback.onBitmap(it) }
//                }
//                null
//            } else {
//                currentBitmap
//            }
//        }
//
//        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
//            return withContext(Dispatchers.IO) {
//                // Block on downloading artwork.
//                Glide.with(context).applyDefaultRequestOptions(glideOptions)
//                    .asBitmap()
//                    .load(uri)
//                    .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
//                    .get()
//            }
//        }
//
//        private fun getDefaultBitmap(context: Context): Bitmap? {
//            return ContextCompat.getDrawable(context, R.drawable.ic_default_cover_bg)
//                ?.toBitmap()
//        }
//    }
}

//private const val NOTIFICATION_LARGE_ICON_SIZE = 144 // px
//
//private val glideOptions = RequestOptions()
//    .fallback(R.drawable.ic_default_cover_bg)
//    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)