package com.github.ygyin.kotlinaudioplayer.utils

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.media.MediaBrowserServiceCompat
import com.github.ygyin.kotlinaudioplayer.MainActivity
import com.github.ygyin.kotlinaudioplayer.extension.id
import com.github.ygyin.kotlinaudioplayer.extension.toMediaSource
import com.github.ygyin.kotlinaudioplayer.extension.toMetadataCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.log

const val KAP_BROWSABLE_ROOT = "/"
const val KAP_EMPTY_ROOT = "@empty@"
private const val MUSIC_USER_AGENT = "music.agent"

class PlaybackService : MediaBrowserServiceCompat(), CoroutineScope by MainScope() {

    private val SESSION_TAG = PlaybackService::class.java.simpleName
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var notificationManager: PlaybackNotificationManager

    private var isForegroundService = false
    private var initialPlaylistItems: List<MediaMetadataCompat> = emptyList()
    private lateinit var initialPlayer: Player

    private val eventListener = EventListener()
    private val dataSourceFactory: DefaultDataSourceFactory by lazy {
        DefaultDataSourceFactory(this, Util.getUserAgent(this, "music.agent"), null)
    }

    // To use Exo Player directly by lazy, use builder to set up parameters
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(eventListener)
        }
    }

    // To use Exo Player's builder to set the content type,
    // let Android know what type of file is playing
    // eg. the volume will become media value.
    private val setAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()


    override fun onCreate() {
        super.onCreate()
        // May have problems without package manager
        val pendingActivity = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java), PendingIntent.FLAG_MUTABLE
        )

        mediaSession = MediaSessionCompat(this, SESSION_TAG).apply {
            setSessionActivity(pendingActivity)
            isActive = true
        }

        playlistRepository = Injector.providePlaylistRepository(this)
        // To be managed by ExoPlayer
        sessionToken = mediaSession.sessionToken
        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(PlaybackPreparer())
        mediaSessionConnector.setQueueNavigator(QueueNavigator(mediaSession))

        playerSwitch(prevPlayer = null, currPlayer = exoPlayer)
    }

    /**
     *  Control the access request from client,
     *  depending whether could be accessed by other Apps or services
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        val isKnownCaller = clientPackageName == packageName
        /*
            App:
            itself -> send back the browser root
            others -> can't use or browse the audio file, but playing function
         */
        return if (isKnownCaller)
            BrowserRoot(KAP_BROWSABLE_ROOT, null)
        else
            BrowserRoot(KAP_EMPTY_ROOT, null)
    }

    /**
     *  Send in the parent id then return the result
     *  Usage: When the cover is clicked, it will show the audio belongs to the album
     *  When it receive the id, it will look for the local repository
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.e(SESSION_TAG, "OnLoadChildren")
        result.detach()
    }

    /**
     * @param metadataList: The list ready to play
     */
    private fun readyToPlay(
        metadataList: List<MediaMetadataCompat>,
        playItem: MediaMetadataCompat?, playWhenReady: Boolean,
        startPosition: Long
    ) {
        val index =
            if (playItem == null) 0
            else metadataList.indexOfFirst { mediaMetadataCompat ->
                mediaMetadataCompat.id == playItem.id
            }

        initialPlaylistItems = metadataList
        initialPlayer.playWhenReady = playWhenReady
        // Reset the player, same as .stop(true)
        initialPlayer.stop()
        initialPlayer.clearMediaItems()

        if (initialPlayer == exoPlayer) {
            val audioSource = metadataList.toMediaSource(dataSourceFactory)
            // Same as prepare(audioSource)
            exoPlayer.setMediaSource(audioSource)
            exoPlayer.prepare()
            exoPlayer.seekTo(index, startPosition)
        }
    }

    private fun playerSwitch(prevPlayer: Player?, currPlayer: Player) {
        if (prevPlayer == currPlayer)
            return

        initialPlayer = currPlayer
        if (prevPlayer != null) {
            val playbackState = prevPlayer.playbackState
            if (initialPlaylistItems.isEmpty()) {
                currPlayer.stop()
                currPlayer.clearMediaItems()
            } else if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                readyToPlay(
                    metadataList = initialPlaylistItems,
                    playItem = initialPlaylistItems[prevPlayer.currentMediaItemIndex],
                    playWhenReady = prevPlayer.playWhenReady,
                    startPosition = prevPlayer.contentPosition
                )
            }
        }
        mediaSessionConnector.setPlayer(currPlayer)
        prevPlayer?.stop()
        prevPlayer?.clearMediaItems()
    }

    private inner class PlaybackPreparer : MediaSessionConnector.PlaybackPreparer {

        override fun onCommand(
            player: Player,
            command: String,
            extras: Bundle?,
            cb: ResultReceiver?
        ): Boolean = false

        // Set up the actions supporting play back
        override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                    PlaybackStateCompat.ACTION_PLAY_FROM_URI

        // Only use MediaId to get audio from playlistRepository
        // Them use ExoPlayer to play the audio
        override fun onPrepareFromMediaId(
            mediaId: String,
            playWhenReady: Boolean,
            extras: Bundle?
        ) {
            launch {
                val playItem = playlistRepository.getMusic().find { item ->
                    item.id.toString() == mediaId
                }?.toMetadataCompat()
                // Music -> MediaMetadataCompat
                // Media session and controller need that
                val playlist = playlistRepository.getMusic().toMetadataCompat()

                if (playItem == null)
                    Log.w(SESSION_TAG, "Media ID not found: $mediaId")
                else
                    readyToPlay(playlist, playItem, playWhenReady, 0)
            }
        }

        override fun onPrepare(playWhenReady: Boolean) = Unit

        // It will be invoked when Google assistant send messages
        override fun onPrepareFromSearch(query: String, playWhenReady: Boolean, extras: Bundle?) =
            Unit

        override fun onPrepareFromUri(uri: Uri, playWhenReady: Boolean, extras: Bundle?) = Unit
    }

    private inner class EventListener : Player.Listener {
//        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//            when (playbackState) {
//                Player.STATE_READY,
//                Player.STATE_BUFFERING -> {
//                    TODO("Show playback notification")
//                }
//            }
//            else -> {
//                  TODO("Hide playback notification")
//            }
//        }

        override fun onPlayerError(error: PlaybackException) {
            Toast.makeText(applicationContext, "Unexpected error", Toast.LENGTH_LONG).show()
        }
    }

    private inner class QueueNavigator(mediaSession: MediaSessionCompat) :
        TimelineQueueNavigator(mediaSession) {
        // Let the notification displays the description of audio
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return initialPlaylistItems[windowIndex].description
        }
    }

}