package com.pairshot.feature.camera.ui.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 카메라 권한을 요청하고 허용된 경우 content를 표시하는 공통 게이트.
 * Before(CameraScreen)와 After(AfterCameraScreen) 모두에서 사용한다.
 *
 * 권한이 거부된 경우: 설정 화면 유도 UI 표시
 * 권한 설명이 필요한 경우: 이유 설명 UI 표시
 * 권한이 허용된 경우: content 표시
 */
@Composable
fun CameraPermissionGate(
    onNavigateBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED,
        )
    }
    var showRationale by remember { mutableStateOf(false) }
    var permissionPermanentlyDenied by remember { mutableStateOf(false) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                hasCameraPermission = true
            } else {
                val shouldShowRationale =
                    activity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
                    } ?: false
                if (shouldShowRationale) {
                    showRationale = true
                } else {
                    permissionPermanentlyDenied = true
                }
            }
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            val shouldShowRationale =
                activity?.let {
                    ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
                } ?: false
            if (shouldShowRationale) {
                showRationale = true
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    when {
        hasCameraPermission -> {
            content()
        }

        permissionPermanentlyDenied -> {
            PermissionDeniedContent(
                onOpenSettings = {
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    context.startActivity(intent)
                },
                onNavigateBack = onNavigateBack,
            )
        }

        showRationale -> {
            PermissionRationaleContent(
                onRequestPermission = {
                    showRationale = false
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onNavigateBack = onNavigateBack,
            )
        }

        else -> {
            // 권한 요청 중 — 빈 검은 화면 유지
        }
    }
}

@Composable
private fun PermissionRationaleContent(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "카메라 권한 필요",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "PairShot은 사진 촬영을 위해 카메라 접근 권한이 필요합니다.",
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRequestPermission) {
            Text(text = "권한 허용")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onNavigateBack) {
            Text(text = "취소", color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onOpenSettings: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "카메라 권한이 거부되었습니다",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "설정 앱에서 PairShot의 카메라 권한을 직접 허용해 주세요.",
            color = Color.White.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onOpenSettings) {
            Text(text = "설정으로 이동")
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onNavigateBack) {
            Text(text = "취소", color = Color.White.copy(alpha = 0.6f))
        }
    }
}
