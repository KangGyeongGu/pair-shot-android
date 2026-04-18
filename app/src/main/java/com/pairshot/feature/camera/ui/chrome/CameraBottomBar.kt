package com.pairshot.feature.camera.ui.chrome

import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pairshot.feature.camera.ui.component.ShutterButton
import java.io.File

/**
 * 카메라 하단 바 — 렌즈 전환, 설정, 셔터 버튼.
 *
 * @param tempFilePrefix 임시 캡처 파일 접두어 (Before: "capture_", After: "after_")
 * @param onImageSaved 캡처 성공 시 savedUri(String) 전달
 * @param onCaptureError 캡처 실패 시 errorMessage 전달
 */
@Composable
internal fun CameraBottomBar(
    imageCapture: ImageCapture,
    isSaving: Boolean,
    shutterEnabled: Boolean,
    shutterPlayer: MediaPlayer?,
    audioManager: AudioManager,
    tempFilePrefix: String,
    height: Dp,
    onToggleLens: () -> Unit,
    onToggleSettings: () -> Unit,
    onShowBlackout: () -> Unit,
    onImageSaved: (String) -> Unit,
    onCaptureError: (String) -> Unit,
) {
    val context = LocalContext.current

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .background(Color.Black)
                .padding(horizontal = 32.dp),
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onToggleLens) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "카메라 전환",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            IconButton(onClick = onToggleSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "설정",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        ShutterButton(
            onClick = {
                shutterPlayer?.let { player ->
                    val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val ratio = if (max > 0) current.toFloat() / max else 0f
                    val vol = ratio * 0.10f
                    player.setVolume(vol, vol)
                    if (player.isPlaying) player.seekTo(0) else player.start()
                }
                onShowBlackout()

                val tempDir = File(context.cacheDir, "temp").also { it.mkdirs() }
                val tempFile = File(tempDir, "${tempFilePrefix}${System.currentTimeMillis()}.jpg")
                val outputFileOptions =
                    ImageCapture.OutputFileOptions
                        .Builder(tempFile)
                        .build()
                imageCapture.takePicture(
                    outputFileOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(exception: ImageCaptureException) {
                            tempFile.delete()
                            onCaptureError(exception.message ?: "촬영 실패")
                        }

                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val savedUri =
                                outputFileResults.savedUri
                                    ?: Uri.fromFile(tempFile)
                            onImageSaved(savedUri.toString())
                        }
                    },
                )
            },
            enabled = !isSaving && shutterEnabled,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
