package com.pairshot.feature.pair.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.pairshot.core.ui.component.MarqueeTitleText
import com.pairshot.core.ui.component.PairShotTopMenu
import com.pairshot.core.ui.component.PairShotTopMenuDivider
import com.pairshot.core.ui.component.PairShotTopMenuItemText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GalleryTopBar(
    projectName: String,
    selectionMode: Boolean,
    selectedCount: Int,
    totalCount: Int,
    showMoreMenu: Boolean,
    onExitSelectionMode: () -> Unit,
    onNavigateBack: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onShowMoreMenu: () -> Unit,
    onDismissMoreMenu: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onShowRenameDialog: () -> Unit,
    onShowProjectDeleteDialog: () -> Unit,
) {
    TopAppBar(
        title = {
            if (selectionMode) {
                Text(
                    text = "${selectedCount}개 선택됨",
                    style = MaterialTheme.typography.titleLarge,
                )
            } else {
                MarqueeTitleText(text = projectName)
            }
        },
        navigationIcon = {
            if (selectionMode) {
                IconButton(onClick = onExitSelectionMode) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "선택 해제",
                    )
                }
            } else {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "뒤로가기",
                    )
                }
            }
        },
        actions = {
            if (selectionMode) {
                val allSelected = selectedCount >= totalCount && totalCount > 0
                TextButton(onClick = if (allSelected) onDeselectAll else onSelectAll) {
                    Text(text = if (allSelected) "전체해제" else "전체선택")
                }
            } else {
                Box {
                    IconButton(onClick = onShowMoreMenu) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
                        )
                    }
                    PairShotTopMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = onDismissMoreMenu,
                    ) {
                        DropdownMenuItem(
                            text = {
                                PairShotTopMenuItemText(
                                    title = "선택",
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                onDismissMoreMenu()
                                onEnterSelectionMode()
                            },
                        )
                        PairShotTopMenuDivider()
                        DropdownMenuItem(
                            text = {
                                PairShotTopMenuItemText(
                                    title = "프로젝트 수정",
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                onDismissMoreMenu()
                                onShowRenameDialog()
                            },
                        )
                        PairShotTopMenuDivider()
                        DropdownMenuItem(
                            text = {
                                PairShotTopMenuItemText(
                                    title = "프로젝트 삭제",
                                    titleColor = MaterialTheme.colorScheme.error,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                onDismissMoreMenu()
                                onShowProjectDeleteDialog()
                            },
                        )
                    }
                }
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
    )
}
