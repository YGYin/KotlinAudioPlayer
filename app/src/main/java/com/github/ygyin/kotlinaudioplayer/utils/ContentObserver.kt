package com.github.ygyin.kotlinaudioplayer.utils

import android.database.ContentObserver
import android.net.Uri
import android.content.ContentResolver
import android.os.Handler

// TODO: .registerObserver
fun ContentResolver.registerObserver(
    uri: Uri,
    observer: (changes: Boolean) -> Unit
): ContentObserver {
    val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(changes: Boolean) {
            observer(changes)
        }
    }

    registerContentObserver(uri, true, contentObserver)
    return contentObserver
}