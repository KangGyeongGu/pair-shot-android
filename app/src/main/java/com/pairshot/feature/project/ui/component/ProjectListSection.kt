package com.pairshot.feature.project.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.designsystem.Success
import com.pairshot.core.domain.model.Project
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
    onClick: () -> Unit,
    onToggleSelection: () -> Unit,
) {
    val shape = MaterialTheme.shapes.small
    val rowModifier =
        when {
            selectionMode && isSelected -> {
                Modifier
                    .clip(shape)
                    .border(BorderStroke(2.dp, Success), shape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            }

            selectionMode -> {
                Modifier
                    .clip(shape)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape)
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
                    onClick = onClick,
                    onLongClick = onToggleSelection,
                ).then(rowModifier)
                .padding(
                    horizontal = PairShotSpacing.cardPadding,
                    vertical = PairShotSpacing.cardPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${project.pairCount}쌍 · 완료 ${project.completedCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatTimestamp(project.updatedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!selectionMode) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            ProjectGroupMode.NONE to "그룹 없음",
            ProjectGroupMode.CREATED_DATE to "생성일",
            ProjectGroupMode.UPDATED_DATE to "최근 작업일",
        )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = PairShotSpacing.screenPadding),
        horizontalArrangement = Arrangement.spacedBy(PairShotSpacing.cardPadding),
    ) {
        options.forEach { (mode, label) ->
            val selected = mode == groupMode
            Column(
                modifier = Modifier.clickable { onGroupModeChange(mode) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color =
                        if (selected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Spacer(modifier = Modifier.size(4.dp))
                Spacer(
                    modifier =
                        Modifier
                            .width(28.dp)
                            .height(2.dp)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                },
                            ),
                )
            }
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
                .padding(horizontal = PairShotSpacing.screenPadding),
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
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column {
            projects.forEachIndexed { index, project ->
                ProjectItem(
                    project = project,
                    selectionMode = selectionMode,
                    isSelected = project.id in selectedIds,
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
        ProjectGroupMode.NONE -> {
            listOf(
                ProjectDisplayGroup(
                    key = "all",
                    label = null,
                    projects = projects.sortedByDescending { it.updatedAt },
                ),
            )
        }

        ProjectGroupMode.CREATED_DATE,
        ProjectGroupMode.UPDATED_DATE,
        -> {
            val grouped =
                projects.groupBy { project ->
                    val millis = if (mode == ProjectGroupMode.CREATED_DATE) project.createdAt else project.updatedAt
                    Instant
                        .ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }

            grouped.entries
                .sortedByDescending { it.key }
                .map { (date, items) ->
                    ProjectDisplayGroup(
                        key = date.toString(),
                        label = formatGroupDate(date),
                        projects = items.sortedByDescending { it.updatedAt },
                    )
                }
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

private fun formatTimestamp(timestampMillis: Long): String =
    Instant
        .ofEpochMilli(timestampMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
