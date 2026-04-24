package com.pairshot.core.coupon.data.repository

import com.pairshot.core.coupon.local.CouponPreferencesSource
import com.pairshot.core.coupon.local.DeviceHashProvider
import com.pairshot.core.coupon.remote.CouponActivationApi
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ComputeNewExpiryTest {
    private lateinit var repository: CouponRepositoryImpl

    private val now = 1_000_000_000L
    private val dayMs = 24L * 60L * 60L * 1000L

    @Before
    fun setUp() {
        repository = CouponRepositoryImpl(mockk<CouponActivationApi>(), mockk<CouponPreferencesSource>(), mockk<DeviceHashProvider>())
    }

    @Test
    fun `no prior coupon, new 30-day — returns now plus 30 days`() {
        val result =
            repository.computeNewExpiry(
                current = null,
                currentIsUnlimited = false,
                newDurationDays = 30L,
                now = now,
            )
        assertEquals(now + 30L * dayMs, result)
    }

    @Test
    fun `5 days remaining, new 7-day — returns now plus 12 days`() {
        val current = now + 5L * dayMs
        val result =
            repository.computeNewExpiry(
                current = current,
                currentIsUnlimited = false,
                newDurationDays = 7L,
                now = now,
            )
        assertEquals(now + 12L * dayMs, result)
    }

    @Test
    fun `30 days remaining, new unlimited — returns null`() {
        val current = now + 30L * dayMs
        val result =
            repository.computeNewExpiry(
                current = current,
                currentIsUnlimited = false,
                newDurationDays = null,
                now = now,
            )
        assertNull(result)
    }

    @Test
    fun `current unlimited, new 30-day — returns null (unlimited preserved)`() {
        val result =
            repository.computeNewExpiry(
                current = null,
                currentIsUnlimited = true,
                newDurationDays = 30L,
                now = now,
            )
        assertNull(result)
    }

    @Test
    fun `already expired, new 7-day — returns now plus 7 days`() {
        val current = now - 1L * dayMs
        val result =
            repository.computeNewExpiry(
                current = current,
                currentIsUnlimited = false,
                newDurationDays = 7L,
                now = now,
            )
        assertEquals(now + 7L * dayMs, result)
    }

    @Test
    fun `current unlimited, new unlimited — returns null`() {
        val result =
            repository.computeNewExpiry(
                current = null,
                currentIsUnlimited = true,
                newDurationDays = null,
                now = now,
            )
        assertNull(result)
    }
}
