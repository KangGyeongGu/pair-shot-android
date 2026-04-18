package com.pairshot.core.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.pairshot.core.designsystem.ModalShape

// 타입별 폭 규격
@Composable
fun confirmDialogWidth(): Dp {
    val sw = LocalConfiguration.current.screenWidthDp.dp
    return (sw - 32.dp).coerceAtMost(360.dp)
}

@Composable
fun optionDialogWidth(): Dp {
    val sw = LocalConfiguration.current.screenWidthDp.dp
    return (sw - 32.dp).coerceAtMost(480.dp)
}

@Composable
fun inputDialogWidth(): Dp {
    val sw = LocalConfiguration.current.screenWidthDp.dp
    return (sw - 32.dp).coerceAtMost(480.dp)
}

/**
 * PairShot 공통 다이얼로그 래퍼.
 * - 라운드 12dp, 경계선 1dp outlineVariant
 * - 스크림: Light 0.45 / Dark 0.62 (AlertDialog 기본 동작에서 추가 오버레이로 구현 불가 — 표준 스크림 사용)
 */
@Composable
fun PairShotDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        modifier = modifier,
        shape = ModalShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = if (isSystemInDarkTheme()) 14.dp else 8.dp,
        properties = properties,
    )
}
