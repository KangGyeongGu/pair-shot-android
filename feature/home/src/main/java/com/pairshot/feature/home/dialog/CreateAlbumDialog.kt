package com.pairshot.feature.home.dialog

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pairshot.core.infra.location.LocationResult
import com.pairshot.core.ui.component.PairShotDialog

@Composable
fun CreateAlbumDialog(
    currentLocation: LocationResult?,
    onFetchLocation: () -> Unit,
    onConfirm: (name: String, address: String?, latitude: Double?, longitude: Double?) -> Unit,
    onDismiss: () -> Unit,
) {
    var albumName by remember { mutableStateOf(currentLocation?.shortAddress ?: "") }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
        ) { grants ->
            val granted =
                grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (granted) onFetchLocation()
        }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        } else {
            onFetchLocation()
        }
    }

    LaunchedEffect(currentLocation) {
        if (currentLocation != null && albumName.isBlank()) {
            albumName = currentLocation.shortAddress ?: ""
        }
    }

    PairShotDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "앨범 생성",
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            val locationAddress = currentLocation?.address
            Column(modifier = Modifier.imePadding()) {
                Text(
                    text = "앨범 이름을 입력하세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = albumName,
                    onValueChange = { albumName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "앨범 이름",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    supportingText =
                        if (locationAddress != null) {
                            {
                                Text(
                                    text = locationAddress,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        } else {
                            null
                        },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        albumName.trim(),
                        currentLocation?.address,
                        currentLocation?.latitude,
                        currentLocation?.longitude,
                    )
                },
                enabled = albumName.isNotBlank(),
            ) {
                Text(
                    text = "확인",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "취소",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}
