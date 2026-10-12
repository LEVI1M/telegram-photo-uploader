package com.worm.photouploader

import android.content.Context
import android.net.Uri
import android.provider.MediaStore

object MediaReader {

    fun getLatestImages(context: Context, limit: Int = 2): List<Uri> {
        val uris = mutableListOf<Uri>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            var count = 0
            while (it.moveToNext() && count < limit) {
                val id = it.getLong(idColumn)
                val uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                uris.add(uri)
                count++
            }
        }
        return uris
    }
}
