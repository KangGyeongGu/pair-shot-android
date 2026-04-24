package com.pairshot.feature.camera.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.ui.graphics.vector.ImageVector
import com.pairshot.feature.camera.R

enum class RotationHintDirection(
    val icon: ImageVector,
    @StringRes val messageRes: Int,
    @StringRes val descRes: Int,
    val angleSign: Float,
) {
    LEFT(
        icon = Icons.Filled.RotateLeft,
        messageRes = R.string.camera_hint_rotate_left_message,
        descRes = R.string.camera_hint_rotate_left_desc,
        angleSign = -1f,
    ),
    RIGHT(
        icon = Icons.Filled.RotateRight,
        messageRes = R.string.camera_hint_rotate_right_message,
        descRes = R.string.camera_hint_rotate_right_desc,
        angleSign = 1f,
    ),
}
