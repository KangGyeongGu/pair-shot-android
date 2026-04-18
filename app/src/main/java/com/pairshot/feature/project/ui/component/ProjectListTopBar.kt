package com.pairshot.feature.project.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.Modifier
import com.pairshot.core.ui.component.PairShotTopMenu
import com.pairshot.core.ui.component.PairShotTopMenuDivider
import com.pairshot.core.ui.component.PairShotTopMenuItemText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectListTopBar(
    selectionMode: Boolean,
    selectedCount: Int,
    showTopMenu: Boolean,
    onExitSelectionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onShowTopMenu: () -> Unit,
    onDismissTopMenu: () -> Unit,
    onEnterSelectionMode: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = if (selectionMode) "${selectedCount}개 선택됨" else "PairShot",
                style =
                    if (selectionMode) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.headlineMedium
                    },
            )
        },
        navigationIcon = {
            if (selectionMode) {
                IconButton(onClick = onExitSelectionMode) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "선택 해제",
                    )
                }
            }
        },
        actions = {
            if (selectionMode) {
                TextButton(onClick = onSelectAll) {
                    Text(text = "전체선택")
                }
            } else {
                Box {
                    IconButton(onClick = onShowTopMenu) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "메뉴",
                        )
                    }
                    PairShotTopMenu(
                        expanded = showTopMenu,
                        onDismissRequest = onDismissTopMenu,
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
                                onDismissTopMenu()
                                onEnterSelectionMode()
                            },
                        )
                        PairShotTopMenuDivider()
                        DropdownMenuItem(
                            text = {
                                PairShotTopMenuItemText(
                                    title = "앱 설정",
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            onClick = {
                                onDismissTopMenu()
                                onNavigateToSettings()
                            },
                        )
                    }
                }
            }
        },
        modifier = Modifier.statusBarsPadding(),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
    )
}
