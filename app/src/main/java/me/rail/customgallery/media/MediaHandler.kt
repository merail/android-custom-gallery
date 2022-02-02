package me.rail.customgallery.media

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import me.rail.customgallery.models.Image
import me.rail.customgallery.models.Media
import me.rail.customgallery.models.Video


class MediaHandler {
    fun findMedia(context: Context) {
        val bucketFileColumn = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaStore.Files.FileColumns.DATA
        } else {
            MediaStore.Files.FileColumns.BUCKET_DISPLAY_NAME
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            bucketFileColumn,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE
        )

        val sortOrder = "date_added DESC"

        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        val queryUri = MediaStore.Files.getContentUri("external")

        val cursor = context.contentResolver.query(
            queryUri,
            projection,
            selection,
            null,
            sortOrder
        ) ?: return

        MediaStorage.setMediasCount(cursor.count)
        MediaStorage.setImagesCount()
        MediaStorage.setVideosCount()

        if (cursor.moveToFirst()) {
            var id: Long
            var uri: Uri
            var name: String
            var bucket: String

            val idColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val bucketColumn = cursor.getColumnIndex(bucketFileColumn)
            val typeColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)

            do {
                id = cursor.getLong(idColumn)
                uri = Uri.withAppendedPath(queryUri, "" + id)
                name = cursor.getString(nameColumn)
                bucket = cursor.getString(bucketColumn)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val pathList = bucket.split("/")
                    bucket = pathList[pathList.lastIndex - 1]
                }
                val type = cursor.getInt(typeColumn)

                val media: Media
                if (type == 1) {
                    media = Image(uri, name)
                    MediaStorage.addImage(media)
                    MediaStorage.addImageToAlbum(bucket, media)
                } else {
                    media = Video(uri, name)
                    MediaStorage.addVideo(media)
                    MediaStorage.addVideoToAlbum(bucket, media)
                }
                MediaStorage.addMedia(media)
                MediaStorage.addMediaToAlbum(bucket, media)
            } while (cursor.moveToNext())
        }

        cursor.close()
    }
}