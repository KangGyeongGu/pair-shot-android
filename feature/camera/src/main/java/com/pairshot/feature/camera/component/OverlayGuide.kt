package com.pairshot.feature.camera.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.exifinterface.media.ExifInterface
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OverlayGuide(
    imageUri: String?,
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    if (imageUri == null) return

    val context = LocalContext.current
    var preparedBitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(imageUri) {
        preparedBitmap =
            withContext(Dispatchers.IO) {
                loadAndRotate(context, imageUri)
            }
    }

    preparedBitmap?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = modifier.alpha(alpha),
        )
    }
}

private suspend fun loadAndRotate(
    context: Context,
    uriString: String,
): Bitmap? {
    val imageLoader = ImageLoader(context)
    val request =
        ImageRequest
            .Builder(context)
            .data(uriString)
            .build()

    val result = imageLoader.execute(request)
    if (result !is SuccessResult) return null

    val bitmap = (result.image as? BitmapImage)?.bitmap ?: return null

    val rotation = calculateOverlayRotation(context, uriString)
    if (rotation == 0f) return bitmap

    val matrix = Matrix().apply { postRotate(rotation) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun calculateOverlayRotation(
    context: Context,
    uriString: String,
): Float {
    val sensorOrientation = getSensorOrientation(context)
    val exifDegrees = readExifDegrees(context, uriString)
    return (sensorOrientation - exifDegrees).toFloat()
}

private fun getSensorOrientation(context: Context): Int {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val backCameraId =
        cameraManager.cameraIdList.firstOrNull { id ->
            val chars = cameraManager.getCameraCharacteristics(id)
            chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: return 90
    return cameraManager
        .getCameraCharacteristics(backCameraId)
        .get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 90
}

private fun readExifDegrees(
    context: Context,
    uriString: String,
): Int {
    val uri = Uri.parse(uriString)
    val inputStream = context.contentResolver.openInputStream(uri) ?: return 0
    return inputStream.use { stream ->
        val orientation =
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }
}
