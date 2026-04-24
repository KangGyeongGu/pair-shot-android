package com.pairshot.core.coupon.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pairshot.core.coupon.R
import com.pairshot.core.coupon.domain.ActivationResult

@Composable
fun CouponRegisterDialog(
    activationState: CouponActivationUiState,
    initialCode: String = "",
    onActivate: (String) -> Unit,
    onScanQr: () -> Unit,
    onDismiss: () -> Unit,
) {
    var code by remember(initialCode) { mutableStateOf(initialCode) }

    LaunchedEffect(initialCode) {
        if (initialCode.isNotBlank()) code = initialCode
    }

    val isLoading = activationState is CouponActivationUiState.Loading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(text = stringResource(R.string.coupon_dialog_title))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.coupon_dialog_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onScanQr,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.coupon_dialog_scan_qr))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(R.string.coupon_dialog_or_separator),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    placeholder = { Text(text = stringResource(R.string.coupon_dialog_input_hint)) },
                    enabled = !isLoading,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                val errorMessage = activationState.errorMessageResId()
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(errorMessage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (isLoading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.coupon_dialog_registering),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onActivate(code) },
                enabled = !isLoading && code.isNotBlank(),
            ) {
                Text(text = stringResource(R.string.coupon_dialog_register))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text(text = stringResource(R.string.coupon_dialog_cancel))
            }
        },
    )
}

private fun CouponActivationUiState.errorMessageResId(): Int? =
    when (this) {
        is CouponActivationUiState.Failure -> {
            when (failure) {
                ActivationResult.Failure.InvalidFormat -> R.string.coupon_error_invalid_format
                ActivationResult.Failure.InvalidSignature -> R.string.coupon_error_invalid_signature
                ActivationResult.Failure.NotFound -> R.string.coupon_error_not_found
                ActivationResult.Failure.AlreadyUsedOnAnotherDevice -> R.string.coupon_error_already_used
                ActivationResult.Failure.Revoked -> R.string.coupon_error_revoked
                ActivationResult.Failure.NetworkError -> R.string.coupon_error_network
                ActivationResult.Failure.UnknownError -> R.string.coupon_error_unknown
            }
        }

        else -> {
            null
        }
    }
