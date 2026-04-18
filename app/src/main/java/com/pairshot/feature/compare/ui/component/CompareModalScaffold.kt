package com.pairshot.feature.compare.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
internal fun CompareModalScaffold(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (isDark) 0.62f else 0.45f))
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                }.padding(horizontal = 16.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (isDark) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            MaterialTheme.colorScheme.background
                        },
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 14.dp else 8.dp),
            shape = RoundedCornerShape(14.dp),
            border =
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.65f else 0.35f),
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {})
                    },
        ) {
            content()
        }
    }
}
