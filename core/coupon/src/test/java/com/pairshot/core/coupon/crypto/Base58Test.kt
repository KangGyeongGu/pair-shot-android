package com.pairshot.core.coupon.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.SecureRandom

class Base58Test {
    @Test
    fun `encode empty bytes returns empty string`() {
        assertEquals("", Base58.encode(ByteArray(0)))
    }

    @Test
    fun `decode empty string returns empty bytes`() {
        assertArrayEquals(ByteArray(0), Base58.decode(""))
    }

    @Test
    fun `roundtrip random bytes`() {
        val random = SecureRandom()
        for (size in intArrayOf(1, 16, 32, 64, 94, 128)) {
            val input = ByteArray(size).also { random.nextBytes(it) }
            val encoded = Base58.encode(input)
            val decoded = Base58.decode(encoded)
            assertArrayEquals("size=$size", input, decoded)
        }
    }

    @Test
    fun `roundtrip preserves leading zeros`() {
        val input = byteArrayOf(0, 0, 1, 2, 3)
        val encoded = Base58.encode(input)
        val decoded = Base58.decode(encoded)
        assertArrayEquals(input, decoded)
    }
}
