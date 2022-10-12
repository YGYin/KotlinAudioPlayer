package com.github.ygyin.kotlinaudioplayer.ui.playlist

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.ygyin.kotlinaudioplayer.R
import com.github.ygyin.kotlinaudioplayer.data.Music
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_music_layout.view.*

// Use RecyclerView, working with adapter and ViewHolder
class PlaylistAdapter(private val onClick: (Music) -> Unit) :
    ListAdapter<Music, PlaylistViewHolder>(Music.diffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_music_layout, parent, false)
        return PlaylistViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val musicItem = getItem(position)
        holder.itemView.musicLayout.tag = musicItem

        holder.itemView.musicName.text = musicItem.title
        holder.itemView.artistName.text = musicItem.artist
        // Use Glide to show the cover in the playlist
        // If fail or no cover, show a custom image
        Glide.with(holder.itemView.context)
            .asBitmap()
            .load(musicItem.coverPath)
            .thumbnail(0.33f)
            .centerCrop()
            .error(R.drawable.ic_default_cover)
            .into(object : CustomTarget<Bitmap>() {

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    holder.itemView.musicCoverImage.setImageDrawable(errorDrawable)
                    holder.itemView.musicCoverImage.setBackgroundResource(R.drawable.ic_default_cover_bg)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    TODO("Noting")
                }

                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    holder.itemView.musicCoverImage.setImageBitmap(resource)
                }

            })
    }

}

class PlaylistViewHolder(private val view: View, onClick: (Music) -> Unit) :
    RecyclerView.ViewHolder(view), LayoutContainer {
    override val containerView: View?
        get() = view

    init {
        view.musicLayout.setOnClickListener {
            val music = view.musicLayout.tag as? Music ?: return@setOnClickListener
            onClick(music)
        }
    }

}
