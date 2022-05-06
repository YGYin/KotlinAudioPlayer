package com.github.ygyin.kotlinaudioplayer.utils

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData

class PlaybackServiceConnection(context: Context, serviceComponent: ComponentName) {

    val isConnected = MutableLiveData<Boolean>()
        .apply { postValue(false) }

    val playbackState = MutableLiveData<PlaybackStateCompat>()
        .apply { postValue(EMPTY_PLAYBACK_STATE) }

    val nowPlaying = MutableLiveData<MediaMetadataCompat>()
        .apply { postValue(NOTHING_PLAYING) }

    val repeatMode = MutableLiveData<Int>()
        .apply { postValue(PlaybackStateCompat.REPEAT_MODE_NONE) }

    val shuffleMode = MutableLiveData<Int>()
        .apply { postValue(PlaybackStateCompat.SHUFFLE_MODE_NONE) }

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    val sessionToken: MediaSessionCompat.Token
        get() = mediaBrowser.sessionToken

    lateinit var mediaController: MediaControllerCompat
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    // UI 通过使用 mediaBrowser 作为 controller 来连接 MediaBrowserService,
    // 通过 compat 做向前支援
    val mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        mediaBrowserConnectionCallback,
        null
    ).also {
        it.connect()
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            nowPlaying.postValue(mediaController.metadata)
            playbackState.postValue(mediaController.playbackState)
            repeatMode.postValue(mediaController.repeatMode)
            shuffleMode.postValue(mediaController.shuffleMode)
            isConnected.postValue(true)
        }

        override fun onConnectionSuspended() {
            Log.d("MediaBrowser", "Connection suspended")
            isConnected.postValue(false)
        }

        override fun onConnectionFailed() {
            Log.d("MediaBrowser", "Connection failed")
            isConnected.postValue(false)
        }
    }

    private inner class MediaControllerCallback: MediaControllerCompat.Callback(){
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.postValue(metadata ?: NOTHING_PLAYING)
        }

//        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
//            super.onQueueChanged(queue)
//        }

        override fun onRepeatModeChanged(mode: Int) {
            repeatMode.postValue(mode)
        }

        override fun onShuffleModeChanged(mode: Int) {
            shuffleMode.postValue(mode)
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: PlaybackServiceConnection? = null

        fun getInstance(context: Context, serviceComponent: ComponentName) =
            instance ?: synchronized(this) {
                instance ?: PlaybackServiceConnection(context, serviceComponent)
                    .also { instance = it }
            }
    }
}

@Suppress("PropertyName")
val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

@Suppress("PropertyName")
val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()
