package com.pairshot.feature.pairpreview.route

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pairshot.core.model.RenderProfile
import com.pairshot.core.rendering.PairImageComposer
import com.pairshot.feature.pairpreview.screen.PairPreviewScreen
import com.pairshot.feature.pairpreview.viewmodel.PairPreviewViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private const val ModalEnterDurationMs = 220

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface PairPreviewRenderEntryPoint {
    fun pairImageComposer(): PairImageComposer
}

@Composable
fun PairPreviewRoute(
    onDismiss: () -> Unit,
    onShareSelected: (pairId: Long) -> Unit,
    onNavigateToAfterCamera: (pairId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PairPreviewViewModel = hiltViewModel(),
) {
    val hasCombined by viewModel.hasCombined.collectAsStateWithLifecycle()
    val livePreviewInputs by viewModel.livePreviewInputs.collectAsStateWithLifecycle()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val composer =
        remember(context) {
            EntryPointAccessors
                .fromApplication(
                    context.applicationContext,
                    PairPreviewRenderEntryPoint::class.java,
                ).pairImageComposer()
        }

    var livePreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(livePreviewInputs) {
        val inputs = livePreviewInputs
        val previous = livePreviewBitmap
        val next =
            if (inputs == null || inputs.pair.afterPhotoUri == null) {
                null
            } else {
                runCatching {
                    composer.compose(
                        beforeUri = Uri.parse(inputs.pair.beforePhotoUri),
                        afterUri = Uri.parse(inputs.pair.afterPhotoUri!!),
                        combineConfig = inputs.config,
                        watermarkConfig = inputs.watermark,
                        profile = RenderProfile.PREVIEW,
                    )
                }.getOrNull()
            }
        livePreviewBitmap = next
        if (previous != null && previous !== next && !previous.isRecycled) {
            previous.recycle()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            livePreviewBitmap?.takeIf { !it.isRecycled }?.recycle()
            livePreviewBitmap = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.deleteComplete.collect {
            onDismiss()
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter =
                scaleIn(
                    initialScale = 0.94f,
                    animationSpec = tween(durationMillis = ModalEnterDurationMs),
                ) + fadeIn(animationSpec = tween(durationMillis = ModalEnterDurationMs)),
        ) {
            PairPreviewScreen(
                hasCombined = hasCombined,
                livePreviewBitmap = livePreviewBitmap,
                showDeleteDialog = showDeleteDialog,
                onClose = onDismiss,
                onShareSelected = { onShareSelected(viewModel.pairId) },
                onNavigateToAfterCamera = { onNavigateToAfterCamera(viewModel.pairId) },
                onDeleteRequested = viewModel::showDeleteDialog,
                onDeleteAll = viewModel::deletePair,
                onDeleteCombinedOnly = viewModel::deleteCombinedOnly,
                onDeleteDismissed = viewModel::dismissDeleteDialog,
            )
        }
    }
}
