package com.github.ygyin.kotlinaudioplayer.ui.nowplaying

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.*
import com.github.ygyin.kotlinaudioplayer.R
import com.github.ygyin.kotlinaudioplayer.data.Music
import com.github.ygyin.kotlinaudioplayer.extension.*
import com.github.ygyin.kotlinaudioplayer.utils.EMPTY_PLAYBACK_STATE
import com.github.ygyin.kotlinaudioplayer.utils.NOTHING_PLAYING
import com.github.ygyin.kotlinaudioplayer.utils.PlaybackServiceConnection
import kotlinx.android.synthetic.main.now_playing_main_content.*
import kotlinx.coroutines.*
import kotlin.math.floor


class NowPlayingViewModel(
    private val context: Context,
    playbackServiceConnection: PlaybackServiceConnection
) : ViewModel() {

    data class NowPlayingMetadata(
        val id: String,
        val coverUri: Uri,
        val title: String?,
        val subtitle: String?,
        val playTime: String
    ) {
        companion object {
            /*
               To convert milliseconds to a display of minutes and seconds
             */
            fun timing(context: Context, position: Long): String {
                val totalSeconds = floor(position / 1E3).toInt()
                val minutes = totalSeconds / 60
                val remainingSeconds = totalSeconds - (minutes * 60)
                return if (position < 0) context.getString(R.string.time_unknown)
                else context.getString(R.string.time_format).format(minutes, remainingSeconds)
            }
        }
    }

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE
    val audioMetadata = MutableLiveData<NowPlayingMetadata>()
    val audioPosition = MutableLiveData<Long>().apply {
        postValue(0L)
    }
    val playbackProcess = MutableLiveData<Int>().apply {
        postValue(0)
    }

    val playPauseButton = MutableLiveData<IntArray>()
    val shuffleMode = MutableLiveData<Int>()
    val repeatMode = MutableLiveData<Int>()

    private val shuffleModes = listOf(
        PlaybackStateCompat.SHUFFLE_MODE_NONE,
        PlaybackStateCompat.SHUFFLE_MODE_ALL
    )
    private val repeatModes = listOf(
        PlaybackStateCompat.REPEAT_MODE_NONE,
        PlaybackStateCompat.REPEAT_MODE_ONE,
        PlaybackStateCompat.REPEAT_MODE_ALL
    )

    private var audioDuration = 0L
    private var positionUpdate = true
    private val handler = Handler(Looper.getMainLooper())
    var controller = MediaControllerCompat(context, playbackServiceConnection.sessionToken)


    /*
        To observe the Media metadata compat
        and playback state compat in the PlaybackServiceConnection.
        If changed, to notice updateState, then renew the NowPlayingMetadata
        Also, the NowPlayingMetadata also will be observed by NowPlayingFragment
     */
    private val playbackStateObserver = Observer<PlaybackStateCompat> {
        playbackState = it ?: EMPTY_PLAYBACK_STATE
        val metadata = playbackServiceConnection.nowPlaying.value ?: NOTHING_PLAYING
        playbackStateUpdate(playbackState, metadata)
    }

    private val audioMetadataObserver = Observer<MediaMetadataCompat> {
        playbackStateUpdate(playbackState, it)
        audioDuration = it.duration
    }

    private val shuffleObserver = Observer<Int> {
        shuffleMode.postValue(it)
    }

    private val repeatObserver = Observer<Int> {
        repeatMode.postValue(it)
    }

    private val playbackServiceConnection = playbackServiceConnection.also {
        it.playbackState.observeForever(playbackStateObserver)
        it.nowPlaying.observeForever(audioMetadataObserver)
        it.shuffleMode.observeForever(shuffleObserver)
        it.repeatMode.observeForever(repeatObserver)
        playbackPositionCheck()
    }

    fun skipPrevious() {
        playbackServiceConnection.transportControls.skipToPrevious()
    }

    fun skipNext() {
        playbackServiceConnection.transportControls.skipToNext()
    }

    fun selectShuffleMode() {
        val currentMode = shuffleModes
            .indexOf(playbackServiceConnection.mediaController.shuffleMode)
        val aimMode = when (currentMode) {
            PlaybackStateCompat.SHUFFLE_MODE_NONE -> PlaybackStateCompat.SHUFFLE_MODE_ALL
            PlaybackStateCompat.SHUFFLE_MODE_ALL -> PlaybackStateCompat.SHUFFLE_MODE_NONE
            else -> PlaybackStateCompat.SHUFFLE_MODE_ALL
        }
        playbackServiceConnection.transportControls.setShuffleMode(aimMode)
    }

    fun selectRepeatMode() {
        val currentMode = repeatModes
            .indexOf(playbackServiceConnection.mediaController.repeatMode)
        val aimMode = when (currentMode) {
            PlaybackStateCompat.REPEAT_MODE_NONE -> PlaybackStateCompat.REPEAT_MODE_ONE
            PlaybackStateCompat.REPEAT_MODE_ONE -> PlaybackStateCompat.REPEAT_MODE_ALL
            PlaybackStateCompat.REPEAT_MODE_ALL -> PlaybackStateCompat.REPEAT_MODE_NONE
            else -> PlaybackStateCompat.REPEAT_MODE_ALL
        }
        playbackServiceConnection.transportControls.setRepeatMode(aimMode)
    }

    /*
        Use handler to catch the playing process every 0.1s
        audioProcess: The percentage of playing process, return 0 ~ 100 for seek bar
        audio position: The actual time of playing audio
        !! The handler should be initialized before checking playback position.
     */
    private fun playbackPositionCheck(): Boolean = handler.postDelayed({
        val currPos = playbackState.currentPlayBackPosition
        if (audioPosition.value != currPos) {
            audioPosition.postValue(currPos)
            if (audioDuration > 0)
                playbackProcess.postValue(((currPos * 100 / audioDuration)).toInt())
        }

        if (positionUpdate)
            playbackPositionCheck()
    }, 100L)

    private fun playbackStateUpdate(
        playbackState: PlaybackStateCompat,
        metadata: MediaMetadataCompat
    ) {
        if (metadata.id != null && metadata.duration != 0L) {
            val playingMetadata = NowPlayingMetadata(
                metadata.id!!,
                metadata.displayIconUri,
                metadata.title?.trim(),
                metadata.displaySubtitle?.trim(),
                NowPlayingMetadata.timing(context, metadata.duration)
            )
            this.audioMetadata.postValue(playingMetadata)
        }

        playPauseButton.postValue(
            when (playbackState.isPlaying) {
                true -> intArrayOf(-R.attr.state_play, R.attr.state_pause) //Set pause
                else -> intArrayOf(R.attr.state_play, -R.attr.state_pause) //Set play
            }
        )
    }

    fun seekTo(percent: Int) {
        val state =
            playbackServiceConnection.playbackState.value?.state ?: PlaybackStateCompat.STATE_NONE
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED) {
            val duration =
                playbackServiceConnection.nowPlaying.value?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                    ?: 0
            val position = duration / 100 * percent
            playbackServiceConnection.transportControls.seekTo(position)
        }
    }

    fun isPlaying(): Boolean {
        return (playbackServiceConnection.playbackState.value?.state == PlaybackStateCompat.STATE_PLAYING)
    }

    override fun onCleared() {
        super.onCleared()
        playbackServiceConnection.playbackState.removeObserver(playbackStateObserver)
        playbackServiceConnection.nowPlaying.removeObserver(audioMetadataObserver)

        positionUpdate = false
    }

    class Factory(
        private val context: Context,
        private val playbackServiceConnection: PlaybackServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NowPlayingViewModel(context, playbackServiceConnection) as T
        }
    }

}
