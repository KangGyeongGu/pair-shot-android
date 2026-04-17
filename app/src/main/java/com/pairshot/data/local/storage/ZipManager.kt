package com.pairshot.data.local.storage

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ZipImageEntry(
    val uri: Uri,
    val entryPath: String,
)

@Singleton
class ZipManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * 이미지 항목들을 ZIP으로 묶어 SAF URI에 저장한다.
         *
         * @param entries 압축할 이미지 목록 (URI + ZIP 내부 경로)
         * @param outputUri SAF ACTION_CREATE_DOCUMENT에서 받은 출력 URI
         * @param onProgress (current, total) — 각 파일 처리 후 호출
         */
        suspend fun createZip(
            entries: List<ZipImageEntry>,
            outputUri: Uri,
            onProgress: (current: Int, total: Int) -> Unit,
        ) = withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            resolver.openOutputStream(outputUri)?.use { outputStream ->
                ZipOutputStream(outputStream.buffered()).use { zip ->
                    entries.forEachIndexed { index, entry ->
                        resolver.openInputStream(entry.uri)?.use { input ->
                            zip.putNextEntry(ZipEntry(entry.entryPath))
                            input.copyTo(zip)
                            zip.closeEntry()
                        }
                        onProgress(index + 1, entries.size)
                    }
                }
            } ?: throw IllegalStateException("SAF 출력 스트림을 열 수 없습니다: $outputUri")
        }

        /**
         * 이미지 항목들을 ZIP으로 묶어 지정된 파일 경로에 저장한다 (공유용 임시 파일).
         *
         * @param entries 압축할 이미지 목록 (URI + ZIP 내부 경로)
         * @param outputFile 저장할 파일 (cacheDir 내 임시 경로)
         * @param onProgress (current, total) — 각 파일 처리 후 호출
         */
        suspend fun createZipToFile(
            entries: List<ZipImageEntry>,
            outputFile: java.io.File,
            onProgress: (current: Int, total: Int) -> Unit,
        ) = withContext(Dispatchers.IO) {
            outputFile.parentFile?.mkdirs()
            val resolver = context.contentResolver
            outputFile.outputStream().buffered().use { outputStream ->
                ZipOutputStream(outputStream).use { zip ->
                    entries.forEachIndexed { index, entry ->
                        resolver.openInputStream(entry.uri)?.use { input ->
                            zip.putNextEntry(ZipEntry(entry.entryPath))
                            input.copyTo(zip)
                            zip.closeEntry()
                        }
                        onProgress(index + 1, entries.size)
                    }
                }
            }
        }
    }
