package com.pairshot.feature.project.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.JoinRight
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.designsystem.PairShotTypographyTokens
import com.pairshot.core.domain.project.Project
import com.pairshot.feature.project.ui.viewmodel.ProjectGroupMode
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ProjectItem(
    project: Project,
    selectionMode: Boolean,
    isSelected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
    onToggleSelection: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val cardShape = MaterialTheme.shapes.medium
    val itemShape =
        when {
            isFirst && isLast -> {
                cardShape
            }

            isFirst -> {
                cardShape.copy(
                    bottomStart =
                        androidx.compose.foundation.shape
                            .CornerSize(0.dp),
                    bottomEnd =
                        androidx.compose.foundation.shape
                            .CornerSize(0.dp),
                )
            }

            isLast -> {
                cardShape.copy(
                    topStart =
                        androidx.compose.foundation.shape
                            .CornerSize(0.dp),
                    topEnd =
                        androidx.compose.foundation.shape
                            .CornerSize(0.dp),
                )
            }

            else -> {
                RectangleShape
            }
        }

    val rowModifier =
        when {
            selectionMode && isSelected -> {
                Modifier
                    .clip(itemShape)
                    .border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), itemShape)
            }

            selectionMode -> {
                Modifier
                    .clip(itemShape)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), itemShape)
            }

            else -> {
                Modifier
            }
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        if (selectionMode) {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onClick()
                    },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleSelection()
                    },
                ).then(rowModifier)
                .padding(
                    horizontal = PairShotSpacing.cardPadding,
                    vertical = PairShotSpacing.cardPadding,
                ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            project.address?.let { address ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${project.completedCount}/${project.pairCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.JoinRight,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${project.combinedCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun ProjectGroupFilterRow(
    groupMode: ProjectGroupMode,
    onGroupModeChange: (ProjectGroupMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options =
        listOf(
            ProjectGroupMode.CREATED_DATE to "생성일",
            ProjectGroupMode.UPDATED_DATE to "최근 작업일",
        )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = PairShotSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.cardPadding, Alignment.End),
    ) {
        options.forEach { (mode, label) ->
            val selected = mode == groupMode
            Text(
                text = label,
                style =
                    PairShotTypographyTokens.labelExtraSmall.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    ),
                color =
                    if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.clickable { onGroupModeChange(mode) },
            )
        }
    }
}

@Composable
internal fun ProjectGroupLabel(
    label: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = label,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = PairShotSpacing.screenPadding + PairShotSpacing.iconTextGap),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
internal fun ProjectGroupCard(
    projects: List<Project>,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onProjectClick: (Long) -> Unit,
    onProjectToggleSelection: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = PairShotSpacing.screenPadding),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column {
            projects.forEachIndexed { index, project ->
                ProjectItem(
                    project = project,
                    selectionMode = selectionMode,
                    isSelected = project.id in selectedIds,
                    isFirst = index == 0,
                    isLast = index == projects.lastIndex,
                    onClick = { onProjectClick(project.id) },
                    onToggleSelection = { onProjectToggleSelection(project.id) },
                )
                if (index < projects.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = PairShotSpacing.cardPadding),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

internal data class ProjectDisplayGroup(
    val key: String,
    val label: String?,
    val projects: List<Project>,
)

internal fun groupProjectsForDisplay(
    projects: List<Project>,
    mode: ProjectGroupMode,
): List<ProjectDisplayGroup> {
    if (projects.isEmpty()) return emptyList()

    return when (mode) {
        ProjectGroupMode.CREATED_DATE -> {
            val grouped =
                projects.groupBy { project ->
                    Instant
                        .ofEpochMilli(project.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
            grouped.entries
                .sortedByDescending { it.key }
                .map { (date, items) ->
                    ProjectDisplayGroup(
                        key = date.toString(),
                        label = formatGroupDate(date),
                        projects = items.sortedByDescending { it.createdAt },
                    )
                }
        }

        ProjectGroupMode.UPDATED_DATE -> {
            listOf(
                ProjectDisplayGroup(
                    key = "all",
                    label = null,
                    projects = projects.sortedByDescending { it.updatedAt },
                ),
            )
        }
    }
}

private fun formatGroupDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> "오늘"
        today.minusDays(1) -> "어제"
        else -> date.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    }
}
