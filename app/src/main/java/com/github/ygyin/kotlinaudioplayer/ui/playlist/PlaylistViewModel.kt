package com.github.ygyin.kotlinaudioplayer.ui.playlist

import android.content.ContentResolver
import android.database.ContentObserver
import android.provider.MediaStore
import androidx.lifecycle.*
import com.github.ygyin.kotlinaudioplayer.utils.PlaylistRepository
import com.github.ygyin.kotlinaudioplayer.data.Music
import com.github.ygyin.kotlinaudioplayer.extension.registerObserver
import com.github.ygyin.kotlinaudioplayer.utils.PlaybackServiceConnection
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val contentResolver: ContentResolver,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private var contentObserver: ContentObserver? = null
    private val audioLiveData = MutableLiveData<List<Music>>()
    val audio: LiveData<List<Music>>
        get() = audioLiveData

    // Got a playlist in a coroutine
    private suspend fun queryAudio(): List<Music> =
        playlistRepository.getMusic()

    fun loadAudio() {
        viewModelScope.launch {
            val playlist = queryAudio()
            audioLiveData.postValue(playlist)
            contentObserver?.let {
                contentObserver = contentResolver.registerObserver(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                ) {
                    loadAudio()
                }
            }
        }
    }

// TODO
//    fun playAudio(){
//
//    }

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
    ) : ViewModelProvider.NewInstanceFactory() {

        // <T :ViewModel?>
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PlaylistViewModel(contentResolver, playlistRepository) as T
        }
    }
}