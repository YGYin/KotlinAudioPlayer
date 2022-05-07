package com.github.ygyin.kotlinaudioplayer.ui.playlist

import android.animation.Animator
import android.content.ContentResolver
import android.database.ContentObserver
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.github.ygyin.kotlinaudioplayer.utils.PlaylistRepository
import com.github.ygyin.kotlinaudioplayer.data.Music
import com.github.ygyin.kotlinaudioplayer.extension.*
import com.github.ygyin.kotlinaudioplayer.utils.PlaybackServiceConnection
import kotlinx.coroutines.launch

private const val PLVM_TAG = "PlaylistViewModel"

class PlaylistViewModel(
    private val contentResolver: ContentResolver,
    private val playlistRepository: PlaylistRepository,
    private val playbackServiceConnection: PlaybackServiceConnection
) : ViewModel() {

    private var contentObserver: ContentObserver? = null
    private val _audio = MutableLiveData<List<Music>>()
    val audio: LiveData<List<Music>> get() = _audio

    // Got a playlist in a coroutine


    fun loadAudio() {
        viewModelScope.launch {
            val playlist = queryAudio()
            _audio.postValue(playlist)
            contentObserver?.let {
                contentObserver = contentResolver.registerObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                ) {
                    loadAudio()
                }
            }
        }
    }

    fun playAudio(playItem: Music, pausePermission: Boolean = true) {
        val nowPlaying = playbackServiceConnection.nowPlaying.value
        val transportControls = playbackServiceConnection.transportControls
        val isPrepared = playbackServiceConnection.playbackState.value?.isPrepared ?: false

        if (isPrepared && playItem.id.toString() == nowPlaying?.id) {
            playbackServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying ->
                        if (pausePermission) // Same audio
                            transportControls.pause()
                        else Unit
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(
                            PLVM_TAG,
                            "Playable item clicked but neither play nor pause are enabled!" +
                                    " (mediaId=${playItem.id.toString()})"
                        )
                    }
                }
            }
        } else
            transportControls.playFromMediaId(playItem.id.toString(), null)
    }

    fun playAudioById(id: String) {
        val nowPlaying = playbackServiceConnection.nowPlaying.value
        val transportControls = playbackServiceConnection.transportControls
        val isPrepared = playbackServiceConnection.playbackState.value?.isPrepared ?: false

        if (isPrepared && id == nowPlaying?.id) {
            playbackServiceConnection.playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> transportControls.pause()
                    playbackState.isPlayEnabled -> transportControls.play()
                    else -> {
                        Log.w(PLVM_TAG, "Play and Pause Fault in playAudioById: $id .")
                    }
                }
            }
        } else
            transportControls.playFromMediaId(id, null)
    }

    private suspend fun queryAudio(): List<Music> =
        playlistRepository.getMusic()

    override fun onCleared() {
        contentObserver?.let {
            contentResolver.unregisterContentObserver(it)
        }
    }

    /**
     * @param contentResolver: ContentResolver
     * @param playlistRepository: PlaylistRepository
     * @param playbackServiceConnection: PlaybackServiceConnection
     */
    class Factory(
        private val contentResolver: ContentResolver,
        private val playlistRepository: PlaylistRepository,
        private val playbackServiceConnection: PlaybackServiceConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        // <T :ViewModel?>
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaylistViewModel(
                contentResolver,
                playlistRepository,
                playbackServiceConnection
            ) as T
        }
    }
}