package com.github.ygyin.kotlinaudioplayer.utils

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class PlaybackService:MediaBrowserServiceCompat(), CoroutineScope by MainScope() {

    private val TAG = PlaybackService::class.java.simpleName

    /**
     * Control the access request from client,
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.e(TAG, "OnLoadChildren")
        result.detach()
    }


}