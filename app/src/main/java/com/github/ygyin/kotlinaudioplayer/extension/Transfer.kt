package com.github.ygyin.kotlinaudioplayer.extension

import android.support.v4.media.MediaMetadataCompat
import com.github.ygyin.kotlinaudioplayer.data.Music


fun Music.toMetadataCompat(): MediaMetadataCompat =
    MediaMetadataCompat.Builder().also {
        it.id = id.toString()
        it.mediaUri = uri.toString()
        it.title = title
        it.artist = artist
        it.albumArtUri = coverPath
        it.displayTitle = title
        it.displaySubtitle = artist
        it.displayIconUri = coverPath
        it.displayDescription = album
    }.build()

fun List<Music>.toMetadataCompat(): List<MediaMetadataCompat> =
    this.map { it.toMetadataCompat() }