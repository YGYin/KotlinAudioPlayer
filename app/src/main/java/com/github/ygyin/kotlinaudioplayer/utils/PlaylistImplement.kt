package com.github.ygyin.kotlinaudioplayer.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.LocusId
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.github.ygyin.kotlinaudioplayer.data.Music
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class PlaylistImplement(private val contentResolver: ContentResolver) : PlaylistRepository {

    private val TAG = "MediaSource"
    val ALBUM_ART_ROOT = Uri.parse("content://media/external/audio/albumart")
    override suspend fun getMusic(): List<Music> {
        val music = mutableListOf<Music>()

        // Let the ContentResolver do it in a coroutine
        withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM
            )
            // To get all the audio file
            val selection = null
            val selectionArgs = null
            val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

            // Do the query, getting info by cursor
            contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                Log.d(TAG, "${cursor.count} is found")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri: Uri =
                        ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val album = cursor.getString(albumColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val coverPath = getAlbumCoverPathByAlbumId(contentResolver, albumId)

                    // To store mediaInfo into the instance of data class
                    val musicContent = Music(
                        id = id,
                        uri = uri,
                        title = title,
                        artist = artist,
                        album = album,
                        albumId = albumId,
                        coverPath = coverPath
                    )
                    music += musicContent
                    Log.d(TAG, "Music Content added: $musicContent")
                }
            }
        }
        return music
    }

    private fun getAlbumCoverPathByAlbumId(
        contentResolver: ContentResolver,
        albumId: Long
    ): String {
        val coverUri = ContentUris.withAppendedId(ALBUM_ART_ROOT, albumId)
        try {
            val openInputStream: InputStream? = contentResolver.openInputStream(coverUri)
            openInputStream?.close()
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
        return coverUri.toString()
    }
}