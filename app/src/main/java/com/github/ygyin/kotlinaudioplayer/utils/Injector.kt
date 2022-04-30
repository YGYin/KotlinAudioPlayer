package com.github.ygyin.kotlinaudioplayer.utils

import android.content.ComponentName
import android.content.Context
import com.github.ygyin.kotlinaudioplayer.utils.PlaylistImplement
import com.github.ygyin.kotlinaudioplayer.ui.playlist.PlaylistViewModel


object Injector {

//    private fun providePlaybackServiceConnection(context: Context): PlaybackServiceConnection {
//        val connection = PlaybackServiceConnection.getInstance(
//            context,
//            ComponentName(context, PlaybackService::class.java)
//        )
//        return connection
//    }

    fun providePlaylistRepository(context: Context)=
        PlaylistImplement(context.contentResolver)

    fun providePlaylistViewModel(context: Context): PlaylistViewModel.Factory {
        val contentResolver = context.contentResolver
        return PlaylistViewModel.Factory(contentResolver, providePlaylistRepository(context))
    }



//    fun providePlaylistViewModel(context: Context): PlaylistViewModel.Factory {
//        val contentResolver = context.contentResolver
//        return PlaylistViewModel

//        val playbackServiceConnection = providePlaybackServiceConnection(context)
//        val factory = PlaylistViewModel.Factory(
//            contentResolver,
//            providePlaylistRepository(context),
//            playbackServiceConnection
//        )
//        return factory
//    }

//    fun provideNowPlayingViewModel(context: Context): NowPlayingViewModel.Factory {
//        val musicServiceConnection = providePlaybackServiceConnection(context)
//        val factory = NowPlayingViewModel.Factory(
//            context,
//            musicServiceConnection
//        )
//        return factory
//    }
}