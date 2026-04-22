package com.pairshot.feature.album.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.pairshot.core.ui.component.PairShotTopMenu
import com.pairshot.core.ui.component.PairShotTopMenuDivider
import com.pairshot.core.ui.component.PairShotTopMenuItem
import com.pairshot.core.ui.component.PairShotTopMenuItemText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailTopBar(
    title: String,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onNavigateBack: () -> Unit,
    onExitSelection: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteAlbumClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = if (isSelectionMode) "${selectedCount}개 선택됨" else title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            if (isSelectionMode) {
                IconButton(onClick = onExitSelection) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "선택 취소",
                    )
                }
            } else {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                    )
                }
            }
        },
        actions = {
            if (!isSelectionMode) {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "더보기",
                    )
                }
                PairShotTopMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    PairShotTopMenuItem(
                        text = { PairShotTopMenuItemText(title = "앨범 이름 수정") },
                        onClick = {
                            menuExpanded = false
                            onRenameClick()
                        },
                    )
                    PairShotTopMenuDivider()
                    PairShotTopMenuItem(
                        text = {
                            PairShotTopMenuItemText(
                                title = "앨범 삭제",
                                titleColor = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteAlbumClick()
                        },
                    )
                }
            }
        },
        colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
    )
}
