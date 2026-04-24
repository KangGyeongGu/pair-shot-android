package com.pairshot.core.coupon.data.repository

import com.pairshot.core.coupon.local.CouponPreferencesSource
import com.pairshot.core.coupon.local.DeviceHashProvider
import com.pairshot.core.coupon.local.StoredCouponState
import com.pairshot.core.coupon.remote.CouponActivationApi
import com.pairshot.core.coupon.remote.StatusApiResult
import com.pairshot.core.coupon.remote.dto.StatusRequestDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CouponRepositorySyncStatusTest {
    private val api: CouponActivationApi = mockk()
    private val preferences: CouponPreferencesSource = mockk()
    private val deviceHashProvider: DeviceHashProvider = mockk()

    private lateinit var repository: CouponRepositoryImpl

    private val storedState =
        StoredCouponState(
            latestCouponId = "test-uuid",
            firstActivatedAtEpochMillis = 0L,
            expiresAtEpochMillis = System.currentTimeMillis() + 30L * 24L * 60L * 60L * 1000L,
        )

    @Before
    fun setUp() {
        repository = CouponRepositoryImpl(api, preferences, deviceHashProvider)
        every { deviceHashProvider.deviceHash() } returns "device-hash-hex"
    }

    @Test
    fun `stored null — api not called`() =
        runTest {
            every { preferences.state } returns flowOf(null)

            repository.syncStatus()

            coVerify(exactly = 0) { api.fetchStatus(any()) }
        }

    @Test
    fun `Activated — preferences not cleared`() =
        runTest {
            every { preferences.state } returns flowOf(storedState)
            coEvery {
                api.fetchStatus(StatusRequestDto(couponId = "test-uuid", deviceHash = "device-hash-hex"))
            } returns StatusApiResult.Activated

            repository.syncStatus()

            coVerify(exactly = 0) { preferences.clear() }
        }

    @Test
    fun `Revoked — preferences cleared`() =
        runTest {
            every { preferences.state } returns flowOf(storedState)
            coEvery {
                api.fetchStatus(StatusRequestDto(couponId = "test-uuid", deviceHash = "device-hash-hex"))
            } returns StatusApiResult.Revoked
            coEvery { preferences.clear() } returns Unit

            repository.syncStatus()

            coVerify(exactly = 1) { preferences.clear() }
        }

    @Test
    fun `NotFoundOrForeign — preferences cleared`() =
        runTest {
            every { preferences.state } returns flowOf(storedState)
            coEvery {
                api.fetchStatus(StatusRequestDto(couponId = "test-uuid", deviceHash = "device-hash-hex"))
            } returns StatusApiResult.NotFoundOrForeign
            coEvery { preferences.clear() } returns Unit

            repository.syncStatus()

            coVerify(exactly = 1) { preferences.clear() }
        }

    @Test
    fun `NetworkError — preferences not cleared`() =
        runTest {
            every { preferences.state } returns flowOf(storedState)
            coEvery {
                api.fetchStatus(StatusRequestDto(couponId = "test-uuid", deviceHash = "device-hash-hex"))
            } returns StatusApiResult.NetworkError

            repository.syncStatus()

            coVerify(exactly = 0) { preferences.clear() }
        }

    @Test
    fun `ServerError — preferences not cleared`() =
        runTest {
            every { preferences.state } returns flowOf(storedState)
            coEvery {
                api.fetchStatus(StatusRequestDto(couponId = "test-uuid", deviceHash = "device-hash-hex"))
            } returns StatusApiResult.ServerError

            repository.syncStatus()

            coVerify(exactly = 0) { preferences.clear() }
        }
}
