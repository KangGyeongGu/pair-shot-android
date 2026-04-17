package com.pairshot.ui.compare

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.domain.model.PairStatus
import com.pairshot.ui.component.ImageProfile
import com.pairshot.ui.component.ProfiledAsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPairing: (projectId: Long, pairId: Long) -> Unit = { _, _ -> },
    viewModel: CompareViewModel = hiltViewModel(),
) {
    val pair by viewModel.pair.collectAsStateWithLifecycle()
    val pairNumber by viewModel.pairNumber.collectAsStateWithLifecycle()
    val pairs by viewModel.pairs.collectAsStateWithLifecycle()
    val currentPairId by viewModel.currentPairId.collectAsStateWithLifecycle()
    val isCombining by viewModel.isCombining.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val currentIndex = pairs.indexOfFirst { it.id == currentPairId }
    val canGoPrev = currentIndex > 0
    val canGoNext = currentIndex in 0 until pairs.lastIndex

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        viewModel.deleteComplete.collect { onNavigateBack() }
    }

    LaunchedEffect(Unit) {
        viewModel.retakeReady.collect { selectedPair ->
            onNavigateToPairing(selectedPair.projectId, selectedPair.id)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.combineComplete.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = if (isDark) 0.62f else 0.45f))
                .pointerInput(Unit) {
                    detectTapGestures { onNavigateBack() }
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
                        // 스크림 탭 dismiss가 카드 내부 탭에 반응하지 않도록 소비
                        detectTapGestures(onTap = {})
                    },
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(30.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text =
                            if (pairNumber > 0) {
                                "$pairNumber/${pairs.size}"
                            } else {
                                ""
                            },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                    )
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(30.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "더보기",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            when (pair?.status) {
                                PairStatus.PAIRED -> {
                                    DropdownMenuItem(
                                        text = { Text("합성 이미지 생성") },
                                        onClick = {
                                            menuExpanded = false
                                            viewModel.combinePair()
                                        },
                                        enabled = !isCombining,
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                }

                                PairStatus.COMBINED -> {
                                    DropdownMenuItem(
                                        text = { Text("합성 결과 보기") },
                                        onClick = {
                                            menuExpanded = false
                                            // TODO: 합성 결과 보기 화면 (향후 구현)
                                        },
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                }

                                else -> {}
                            }
                            DropdownMenuItem(
                                text = { Text("After 재촬영") },
                                onClick = {
                                    menuExpanded = false
                                    viewModel.prepareRetake()
                                },
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            DropdownMenuItem(
                                text = { Text(text = "삭제", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteDialog = true
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.size(6.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                ) {
                    Text(
                        text = "Before",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "After",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 5.dp)
                            .pointerInput(pairs, currentPairId) {
                                var dragTotal = 0f
                                val swipeThreshold = 72f
                                detectHorizontalDragGestures(
                                    onDragStart = { dragTotal = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        dragTotal += dragAmount
                                    },
                                    onDragEnd = {
                                        when {
                                            dragTotal > swipeThreshold && canGoPrev -> {
                                                viewModel.selectPair(pairs[currentIndex - 1].id)
                                            }

                                            dragTotal < -swipeThreshold && canGoNext -> {
                                                viewModel.selectPair(pairs[currentIndex + 1].id)
                                            }
                                        }
                                        dragTotal = 0f
                                    },
                                    onDragCancel = { dragTotal = 0f },
                                )
                            },
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ProfiledAsyncImage(
                            data = pair?.beforePhotoUri,
                            profile = ImageProfile.DETAIL,
                            contentDescription = "Before 사진",
                            contentScale = ContentScale.Fit,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(3f / 4f),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        ProfiledAsyncImage(
                            data = pair?.afterPhotoUri,
                            profile = ImageProfile.DETAIL,
                            contentDescription = "After 사진",
                            contentScale = ContentScale.Fit,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(3f / 4f),
                        )
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 1.dp),
                ) {
                    Text(
                        text =
                            pair?.beforeTimestamp?.let {
                                "${fullDateFormat.format(Date(it))} ${timeFormat.format(Date(it))}"
                            } ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text =
                            pair?.afterTimestamp?.let {
                                "${fullDateFormat.format(Date(it))} ${timeFormat.format(Date(it))}"
                            } ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            if (canGoPrev) {
                                viewModel.selectPair(pairs[currentIndex - 1].id)
                            }
                        },
                        enabled = canGoPrev,
                    ) {
                        Text(
                            text = "<",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Text(
                        text =
                            if (pairNumber > 0) {
                                "$pairNumber/${pairs.size}"
                            } else {
                                "-"
                            },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(
                        onClick = {
                            if (canGoNext) {
                                viewModel.selectPair(pairs[currentIndex + 1].id)
                            }
                        },
                        enabled = canGoNext,
                    ) {
                        Text(
                            text = ">",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 0.dp),
                )
            }
        }
    }

    if (isCombining) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("합성 중...") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("이미지를 합성하고 있습니다", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {},
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("페어 삭제") },
            text = { Text("이 페어를 삭제하시겠습니까? Before/After 사진이 모두 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePair()
                    },
                ) {
                    Text(
                        text = "삭제",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            },
        )
    }
}
