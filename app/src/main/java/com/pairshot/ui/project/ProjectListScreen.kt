package com.pairshot.ui.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.ui.theme.PairShotSpacing

@Composable
fun ProjectListScreen(
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProject: (Long) -> Unit = {},
) {
    Scaffold(
        topBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = PairShotSpacing.screenPadding)
                        .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "PairShot",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "설정",
                    )
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.navigationBarsPadding()) {
                Button(
                    onClick = { onNavigateToProject(0L) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PairShotSpacing.screenPadding, vertical = PairShotSpacing.itemGap)
                            .height(52.dp),
                ) {
                    Text(
                        text = "새 프로젝트 만들기",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "프로젝트 목록")
        }
    }
}
