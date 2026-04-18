package com.pairshot.feature.camera.ui.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.pairshot.feature.camera.ui.permission.CameraPermissionGate
import com.pairshot.feature.camera.ui.screen.CameraScreen
import com.pairshot.feature.camera.ui.viewmodel.CameraViewModel

@Composable
fun CameraRoute(
    projectId: Long,
    onNavigateBack: () -> Unit,
    viewModel: CameraViewModel = hiltViewModel(),
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        CameraPermissionGate(onNavigateBack = onNavigateBack) {
            CameraScreen(
                projectId = projectId,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
            )
        }
    }
}
