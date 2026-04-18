package com.pairshot.feature.pair.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.JoinRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.designsystem.PairShotTypographyTokens

@Composable
internal fun GallerySelectionBottomBar(
    selectedCount: Int,
    onCombineSelected: () -> Unit,
    onExportSelected: () -> Unit,
    onShowDeleteDialog: () -> Unit,
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(PairShotSpacing.actionBar)
                    .padding(horizontal = PairShotSpacing.screenPadding),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onCombineSelected,
                        enabled = selectedCount > 0,
                    ) {
                        Icon(
                            imageVector = Icons.Default.JoinRight,
                            contentDescription = "합성",
                        )
                    }
                    Text(
                        text = "합성",
                        style = PairShotTypographyTokens.labelExtraSmall,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onExportSelected,
                        enabled = selectedCount > 0,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "공유",
                        )
                    }
                    Text(
                        text = "공유",
                        style = PairShotTypographyTokens.labelExtraSmall,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = onShowDeleteDialog,
                        enabled = selectedCount > 0,
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = "삭제",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text(
                        text = "삭제",
                        style = PairShotTypographyTokens.labelExtraSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
