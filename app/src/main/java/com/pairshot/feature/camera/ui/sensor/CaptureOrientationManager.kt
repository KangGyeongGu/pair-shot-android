package com.pairshot.feature.camera.ui.sensor

import android.content.Context
import android.view.OrientationEventListener
import android.view.Surface
import androidx.camera.core.ImageCapture

class CaptureOrientationManager(
    context: Context,
    private val imageCapture: ImageCapture,
) {
    private val listener =
        object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val rotation =
                    when (orientation) {
                        in 45..134 -> Surface.ROTATION_270
                        in 135..224 -> Surface.ROTATION_180
                        in 225..314 -> Surface.ROTATION_90
                        else -> Surface.ROTATION_0
                    }
                imageCapture.targetRotation = rotation
            }
        }

    fun start() {
        if (listener.canDetectOrientation()) {
            listener.enable()
        }
    }

    fun stop() {
        listener.disable()
    }
}
