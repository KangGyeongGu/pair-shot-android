package com.pairshot.feature.export.data.service

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareImagePreparer
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun prepareTempDir(name: String): File {
            val dir = File(context.cacheDir, name)
            dir.deleteRecursively()
            dir.mkdirs()
            return dir
        }

        fun prepareShareImageDir(): File {
            val dir = File(context.cacheDir, "share_images")
            dir.deleteRecursively()
            dir.mkdirs()
            return dir
        }

        fun getFileProviderUri(file: File): Uri {
            val authority = "${context.packageName}.fileprovider"
            return FileProvider.getUriForFile(context, authority, file)
        }

        fun copyFromContentUri(
            sourceUri: String,
            destFile: File,
        ) {
            context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
    }
