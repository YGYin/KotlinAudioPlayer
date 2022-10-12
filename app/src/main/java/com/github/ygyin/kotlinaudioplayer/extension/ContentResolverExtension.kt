package com.github.ygyin.kotlinaudioplayer.extension

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler

fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (selfChange: Boolean) -> Unit
): ContentObserver {
    val cteObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            observer(selfChange)
        }
    }
    registerContentObserver(uri, true, cteObserver)
    return cteObserver
}
