package com.pairshot.app.navigation.effect

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.pairshot.core.domain.export.ExportAction
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.io.File

@Composable
fun ExportShareEffect(actions: Flow<ExportAction>) {
    val context = LocalContext.current
    LaunchedEffect(actions) {
        actions.collect { action ->
            runCatching {
                val intent = buildShareIntent(context, action)
                val chooser =
                    Intent.createChooser(intent, null).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(chooser)
            }.onFailure { error ->
                Timber.e(error, "share intent failed")
            }
        }
    }
}

private fun buildShareIntent(
    context: Context,
    action: ExportAction,
): Intent =
    when (action) {
        is ExportAction.ShareImages -> buildSendMultipleIntent(context, action.uris)
        is ExportAction.ShareZip -> buildSendZipIntent(context, action.filePath)
    }

private fun buildSendMultipleIntent(
    context: Context,
    uriStrings: List<String>,
): Intent {
    require(uriStrings.isNotEmpty()) { "no URIs to share" }
    val uris = ArrayList(uriStrings.map { Uri.parse(it) })
    return Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData =
            ClipData.newRawUri("images", uris.first()).apply {
                uris.drop(1).forEach { addItem(ClipData.Item(it)) }
            }
    }
}

private fun buildSendZipIntent(
    context: Context,
    filePath: String,
): Intent {
    val file = File(filePath)
    val authority = "${context.packageName}.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)
    return Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        clipData = ClipData.newRawUri("zip", uri)
    }
}
