package com.pairshot.feature.compare.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.ModalShape
import com.pairshot.core.designsystem.PairShotSpacing

private val CompareModalMaxWidth = 560.dp
private val CompareModalVerticalPadding = 28.dp

@Composable
internal fun CompareModalScaffold(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .background(Color.Black.copy(alpha = 0.52f))
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                }.padding(horizontal = PairShotSpacing.cardPadding, vertical = CompareModalVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = ModalShape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = CompareModalMaxWidth)
                    .wrapContentHeight()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {})
                    },
        ) {
            content()
        }
    }
}
