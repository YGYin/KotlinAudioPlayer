package com.github.ygyin.kotlinaudioplayer.utils

import android.content.ComponentName
import android.content.Context
import com.github.ygyin.kotlinaudioplayer.ui.nowplaying.NowPlayingViewModel
import com.github.ygyin.kotlinaudioplayer.ui.playlist.PlaylistViewModel


object Injector {
    // Provide Play back service
    fun providePlaybackServiceConnection(context: Context): PlaybackServiceConnection {
        return PlaybackServiceConnection.getInstance(
            context,
            ComponentName(context, PlaybackService::class.java)
        )

    }
    // Provide Media Source
    fun providePlaylistRepository(context: Context) =
        PlaylistImplement(context.contentResolver)

    // Provide Playlist View Model
    fun providePlaylistViewModel(context: Context): PlaylistViewModel.Factory {
        val contentResolver = context.contentResolver
        val playbackServiceConnection = providePlaybackServiceConnection(context)
        return PlaylistViewModel.Factory(
            contentResolver,
            providePlaylistRepository(context),
            playbackServiceConnection
        )
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

    fun provideNowPlayingViewModel(context: Context): NowPlayingViewModel.Factory {
        val musicServiceConnection = providePlaybackServiceConnection(context)
        return NowPlayingViewModel.Factory(
            context,
            musicServiceConnection
        )
    }
}