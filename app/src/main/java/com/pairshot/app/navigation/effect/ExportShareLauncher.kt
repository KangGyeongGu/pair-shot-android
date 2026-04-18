package com.pairshot.app.navigation.effect

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.pairshot.ui.export.ExportAction
import kotlinx.coroutines.flow.Flow
import java.io.File

@Composable
fun ExportShareEffect(exportAction: Flow<ExportAction>) {
    val context = LocalContext.current

    LaunchedEffect("exportAction") {
        exportAction.collect { action ->
            val resolver = context.contentResolver
            val intent =
                when (action) {
                    is ExportAction.ShareZip -> {
                        val zipFile = File(action.filePath)
                        val authority = "${context.packageName}.fileprovider"
                        val uri = FileProvider.getUriForFile(context, authority, zipFile)
                        Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            clipData = ClipData.newUri(resolver, "PairShot ZIP", uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }

                    is ExportAction.ShareImages -> {
                        val uris = action.uris.map { Uri.parse(it) }
                        if (uris.size == 1) {
                            Intent(Intent.ACTION_SEND).apply {
                                type = "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, uris.first())
                                clipData = ClipData.newUri(resolver, "PairShot", uris.first())
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        } else {
                            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                type = "image/*"
                                putParcelableArrayListExtra(
                                    Intent.EXTRA_STREAM,
                                    ArrayList(uris),
                                )
                                clipData =
                                    ClipData
                                        .newUri(resolver, "PairShot", uris.first())
                                        .apply {
                                            uris.drop(1).forEach {
                                                addItem(ClipData.Item(it))
                                            }
                                        }
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        }
                    }
                }
            context.startActivity(Intent.createChooser(intent, null))
        }
    }
}
