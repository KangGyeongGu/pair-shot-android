package com.pairshot.ui.compare

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    pairId: Long,
    onNavigateBack: () -> Unit = {},
    onNavigateToPairing: (projectId: Long, pairId: Long) -> Unit = { _, _ -> },
    viewModel: CompareViewModel = hiltViewModel(),
) {
    val pair by viewModel.pair.collectAsStateWithLifecycle()
    val pairNumber by viewModel.pairNumber.collectAsStateWithLifecycle()
    val isCombining by viewModel.isCombining.collectAsStateWithLifecycle()

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        viewModel.deleteComplete.collect {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.retakeReady.collect { pair ->
            onNavigateToPairing(pair.projectId, pair.id)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.combineComplete.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (pairNumber > 0) "#$pairNumber" else "",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
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
                            text = {
                                Text(
                                    text = "삭제",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                showDeleteDialog = true
                            },
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
        ) {
            // Before / After 라벨 (이미지 바로 위)
            Row(modifier = Modifier.fillMaxWidth()) {
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

            // 이미지 영역
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

            // 날짜 시간 라벨 (이미지 바로 아래)
            Row(modifier = Modifier.fillMaxWidth()) {
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
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deletePair()
                }) {
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
