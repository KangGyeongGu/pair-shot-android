package com.pairshot.feature.compare.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
                .background(Color.Black.copy(alpha = 0.45f))
                .pointerInput(Unit) {
                    detectTapGestures { onDismiss() }
                }.padding(horizontal = PairShotSpacing.cardPadding, vertical = CompareModalVerticalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            shape = ModalShape,
            border =
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                ),
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
