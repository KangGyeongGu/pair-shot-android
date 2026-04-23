package com.pairshot

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.PerformanceMetricsState
import com.pairshot.app.navigation.PairShotNavHost
import com.pairshot.app.navigation.SelectionActionViewModel
import com.pairshot.app.navigation.SelectionMessage
import com.pairshot.app.navigation.effect.ExportShareEffect
import com.pairshot.core.designsystem.PairShotSpacing
import com.pairshot.core.designsystem.PairShotTheme
import com.pairshot.core.ui.component.PairShotSnackbar
import com.pairshot.core.ui.component.SnackbarVariant
import com.pairshot.core.ui.component.TopProgressPill
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var jankStats: JankStats

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val metricsStateHolder = PerformanceMetricsState.getHolderForHierarchy(window.decorView)
        jankStats =
            JankStats.createAndTrack(window) { frameData ->
                if (frameData.isJank && BuildConfig.DEBUG) {
                    Log.w("JankStats", frameData.toString())
                }
            }
        metricsStateHolder.state?.putState("screen", "Camera")

        setContent {
            PairShotTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val selectionVm: SelectionActionViewModel = hiltViewModel()
                    val progress by selectionVm.progress.collectAsStateWithLifecycle()
                    var selectionMessage by remember { mutableStateOf<SelectionMessage?>(null) }

                    LaunchedEffect(Unit) {
                        selectionVm.messages.collect { msg -> selectionMessage = msg }
                    }

                    LaunchedEffect(selectionMessage) {
                        if (selectionMessage != null) {
                            delay(2500)
                            selectionMessage = null
                        }
                    }

                    ExportShareEffect(actions = selectionVm.exportAction)

                    Box(modifier = Modifier.fillMaxSize()) {
                        PairShotNavHost(
                            onDestinationChanged = { route ->
                                metricsStateHolder.state?.putState("screen", route)
                            },
                            onShareSelected = selectionVm::shareSelection,
                            onSaveSelectedToDevice = selectionVm::saveSelectionToDevice,
                        )

                        progress?.let { p ->
                            TopProgressPill(
                                label =
                                    pluralStringResource(
                                        R.plurals.progress_label_with_count,
                                        p.total,
                                        p.label.asString(),
                                        p.total,
                                    ),
                                progress = if (p.total > 0) p.current.toFloat() / p.total else 0f,
                                progressText = "${p.current}/${p.total}",
                                modifier =
                                    Modifier
                                        .align(Alignment.TopCenter)
                                        .statusBarsPadding()
                                        .padding(top = PairShotSpacing.snackbarTopOffset),
                            )
                        }

                        selectionMessage?.let { msg ->
                            val variant =
                                when (msg) {
                                    is SelectionMessage.Success -> SnackbarVariant.SUCCESS
                                    is SelectionMessage.Warning -> SnackbarVariant.WARNING
                                    is SelectionMessage.Error -> SnackbarVariant.ERROR
                                }
                            PairShotSnackbar(
                                message = msg.text.asString(),
                                variant = variant,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopCenter)
                                        .statusBarsPadding()
                                        .padding(top = if (progress != null) 80.dp else PairShotSpacing.snackbarTopOffset),
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        jankStats.isTrackingEnabled = true
    }

    override fun onPause() {
        super.onPause()
        jankStats.isTrackingEnabled = false
    }
}
