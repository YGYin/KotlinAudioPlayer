package com.github.ygyin.kotlinaudioplayer.data

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil

data class Music(
    val uri: Uri,
    val id: Long,
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumId: Long?,
    val coverPath: String?
) {
    companion object {
        val diffCallBack = object : DiffUtil.ItemCallback<Music>() {
            override fun areItemsTheSame(oldItem: Music, newItem: Music) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Music, newItem: Music) =
                oldItem == newItem
        }
    }
}
