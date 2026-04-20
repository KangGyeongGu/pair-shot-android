package com.pairshot.feature.project.ui.dialog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.core.ui.component.PairShotDialog
import com.pairshot.feature.project.ui.viewmodel.ProjectViewModel

@Composable
internal fun CreateProjectDialog(
    viewModel: ProjectViewModel,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var name by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val isLocationLoading by viewModel.isLocationLoading.collectAsStateWithLifecycle()
    var showLocationRationale by remember { mutableStateOf(false) }
    var showLocationPermanentlyDenied by remember { mutableStateOf(false) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) {
                viewModel.fetchCurrentLocation()
            } else {
                val shouldShowRationale =
                    activity != null &&
                        (
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                            ) ||
                                ActivityCompat.shouldShowRequestPermissionRationale(
                                    activity,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                )
                        )
                if (shouldShowRationale) {
                    showLocationRationale = true
                } else {
                    showLocationPermanentlyDenied = true
                }
            }
        }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        val hasPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            viewModel.fetchCurrentLocation()
        } else {
            val needsRationale =
                activity != null &&
                    (
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            activity,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                    )
            if (needsRationale) {
                showLocationRationale = true
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }
        }
    }

    if (showLocationRationale) {
        AlertDialog(
            onDismissRequest = { showLocationRationale = false },
            title = {
                Text(
                    text = "위치 권한 필요",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = "사진 촬영 위치를 프로젝트에 기록하기 위해 위치 권한이 필요합니다.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationRationale = false
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    },
                ) {
                    Text("확인", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationRationale = false }) {
                    Text("취소")
                }
            },
        )
    }

    if (showLocationPermanentlyDenied) {
        AlertDialog(
            onDismissRequest = { showLocationPermanentlyDenied = false },
            title = {
                Text(
                    text = "위치 권한이 필요합니다",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = "위치 권한이 거부되었습니다. 설정에서 권한을 허용해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLocationPermanentlyDenied = false
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        context.startActivity(intent)
                    },
                ) {
                    Text("설정 열기", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLocationPermanentlyDenied = false }) {
                    Text("취소")
                }
            },
        )
    }

    val placeholderText = currentLocation?.shortAddress ?: "프로젝트 이름"

    PairShotDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "새 프로젝트",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = placeholderText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    shape = MaterialTheme.shapes.medium,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors =
                        TextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                        ),
                )
                when {
                    currentLocation?.address != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = currentLocation?.address ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }

                    isLocationLoading -> {
                        Text(
                            text = "위치 정보 가져오는 중…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name) }) {
                Text("만들기", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
internal fun RenameProjectDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }

    PairShotDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "이름 변경",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors =
                    TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(name) },
                enabled = name.isNotBlank(),
            ) {
                Text("변경", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
internal fun DeleteProjectDialog(
    projectName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "프로젝트 삭제",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                "'$projectName' 프로젝트를 정말 삭제하시겠습니까?",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
internal fun DeleteSelectedProjectsDialog(
    selectedCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    PairShotDialog(
        onDismissRequest = onDismiss,
        title = { Text("선택 항목 삭제") },
        text = { Text("${selectedCount}개 프로젝트를 삭제하겠습니까?", style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("삭제", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}
