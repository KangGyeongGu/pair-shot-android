package com.pairshot.data.local.storage

import android.app.RecoverableSecurityException
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class DeleteResult {
    data object Success : DeleteResult()

    data object NotFound : DeleteResult()

    data class RecoverablePermission(
        val exception: RecoverableSecurityException,
    ) : DeleteResult()

    data class Failed(
        val exception: Exception,
    ) : DeleteResult()
}

@Singleton
class MediaStoreManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun saveToGallery(
            tempFileUri: Uri,
            projectName: String,
            displayName: String,
        ): Uri {
            val resolver = context.contentResolver

            val imageCollection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            val imageDetails =
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PairShot/$projectName/")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

            val imageUri =
                resolver.insert(imageCollection, imageDetails)
                    ?: throw IllegalStateException("MediaStore insert failed")

            resolver.openInputStream(tempFileUri)?.use { input ->
                resolver.openOutputStream(imageUri)?.use { output ->
                    input.copyTo(output)
                } ?: throw IllegalStateException("Failed to open output stream")
            } ?: throw IllegalStateException("Failed to open input stream")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val updateValues =
                    ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }
                resolver.update(imageUri, updateValues, null, null)
            }

            return imageUri
        }

        fun deleteFromGallery(contentUri: Uri): DeleteResult =
            try {
                val rows = context.contentResolver.delete(contentUri, null, null)
                if (rows > 0) DeleteResult.Success else DeleteResult.NotFound
            } catch (e: Exception) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                    DeleteResult.RecoverablePermission(e)
                } else {
                    DeleteResult.Failed(e)
                }
            }

        fun saveBitmapToGallery(
            bitmap: Bitmap,
            projectName: String,
            displayName: String,
            quality: Int = 95,
        ): Uri {
            val resolver = context.contentResolver

            val imageCollection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            val imageDetails =
                ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PairShot/$projectName/")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

            val imageUri =
                resolver.insert(imageCollection, imageDetails)
                    ?: throw IllegalStateException("MediaStore insert failed")

            resolver.openOutputStream(imageUri)?.use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
            } ?: throw IllegalStateException("Failed to open output stream")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val updateValues =
                    ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }
                resolver.update(imageUri, updateValues, null, null)
            }

            return imageUri
        }
    }
