package com.pairshot.data.local.storage

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * 임시 파일 URI의 이미지를 MediaStore에 저장.
         * Pictures/PairShot/{projectName}/ 경로에 저장.
         * IS_PENDING=1 → 쓰기 → IS_PENDING=0 패턴 사용.
         *
         * @return 저장된 MediaStore URI (content:// scheme)
         */
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
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PairShot/$projectName/")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

        /**
         * Bitmap을 MediaStore에 직접 저장 (합성 이미지용).
         */
        fun deleteFromGallery(contentUri: Uri): Boolean =
            try {
                context.contentResolver.delete(contentUri, null, null) > 0
            } catch (e: Exception) {
                false
            }

        fun saveBitmapToGallery(
            bitmap: Bitmap,
            projectName: String,
            displayName: String,
            quality: Int = 85,
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
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PairShot/$projectName/")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
