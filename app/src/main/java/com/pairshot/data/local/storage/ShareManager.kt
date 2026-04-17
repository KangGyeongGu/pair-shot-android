package com.pairshot.data.local.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * 다중 이미지 공유 Intent 생성 (ACTION_SEND_MULTIPLE)
         * content:// URI 목록을 그대로 전달 (MediaStore URI)
         */
        fun createShareImagesIntent(uris: List<Uri>): Intent =
            if (uris.size == 1) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uris.first())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/jpeg"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }

        /**
         * ZIP 파일 공유 Intent 생성
         * cacheDir의 ZIP → FileProvider URI → ACTION_SEND
         */
        fun createShareZipIntent(zipFile: File): Intent {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, zipFile)
            return Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        /**
         * cacheDir에 임시 ZIP 파일 경로 반환
         * 호출자가 ZipManager로 이 경로에 ZIP을 생성한 뒤 공유
         */
        fun createTempZipFile(projectName: String): File {
            val shareDir = File(context.cacheDir, "share")
            shareDir.mkdirs()
            // 기존 임시 파일 정리
            shareDir.listFiles()?.forEach { it.delete() }
            return File(shareDir, "PairShot_$projectName.zip")
        }
    }
