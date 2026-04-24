package com.pairshot.core.coupon.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.pairshot.core.coupon.R
import com.pairshot.core.coupon.domain.CouponStatus
import com.pairshot.core.ui.component.SettingsItem

private const val MILLIS_PER_DAY: Long = 24L * 60L * 60L * 1000L

@Composable
fun CouponStatusItem(
    status: CouponStatus,
    nowMillis: Long,
    onClick: () -> Unit,
) {
    val trailing = status.toDisplayText(nowMillis = nowMillis)
    SettingsItem(
        label = stringResource(R.string.coupon_item_label),
        trailing = trailing,
        onClick = onClick,
    )
}

@Composable
private fun CouponStatus.toDisplayText(nowMillis: Long): String =
    when (this) {
        CouponStatus.None -> {
            stringResource(R.string.coupon_status_none)
        }

        is CouponStatus.Active -> {
            val expires = expiresAtEpochMillis
            if (expires == null) {
                stringResource(R.string.coupon_status_active_unlimited)
            } else {
                val daysRemaining = computeDaysRemaining(expires, nowMillis)
                pluralStringResource(
                    R.plurals.coupon_status_active_days_remaining,
                    daysRemaining,
                    daysRemaining,
                )
            }
        }

        is CouponStatus.Expired -> {
            stringResource(R.string.coupon_status_expired)
        }
    }

private fun computeDaysRemaining(
    expiresAtEpochMillis: Long,
    nowMillis: Long,
): Int {
    val diff = expiresAtEpochMillis - nowMillis
    if (diff <= 0L) return 0
    val days = (diff + MILLIS_PER_DAY - 1L) / MILLIS_PER_DAY
    return days.toInt()
}
