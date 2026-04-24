package com.pairshot.core.coupon.crypto

internal object Base58 {
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private const val BASE = 58
    private const val ASCII_TABLE_SIZE = 128
    private const val BYTE_MASK = 0xFF
    private const val BYTE_BASE = 256
    private val INDICES: IntArray =
        IntArray(ASCII_TABLE_SIZE) { -1 }.also { arr ->
            ALPHABET.forEachIndexed { index, c -> arr[c.code] = index }
        }

    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""

        var zeros = 0
        while (zeros < input.size && input[zeros].toInt() == 0) zeros++

        val inputCopy = input.copyOf()
        val encoded = CharArray(input.size * 2)
        var outputStart = encoded.size

        var startAt = zeros
        while (startAt < inputCopy.size) {
            val mod = divmod(inputCopy, startAt, BYTE_BASE, BASE)
            if (inputCopy[startAt].toInt() == 0) startAt++
            encoded[--outputStart] = ALPHABET[mod.toInt() and BYTE_MASK]
        }

        while (outputStart < encoded.size && encoded[outputStart] == ALPHABET[0]) outputStart++
        repeat(zeros) { encoded[--outputStart] = ALPHABET[0] }

        return String(encoded, outputStart, encoded.size - outputStart)
    }

    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)

        val input58 = ByteArray(input.length)
        for (i in input.indices) {
            val c = input[i]
            val digit = if (c.code < ASCII_TABLE_SIZE) INDICES[c.code] else -1
            require(digit >= 0) { "Illegal Base58 character: $c" }
            input58[i] = digit.toByte()
        }

        var zeros = 0
        while (zeros < input58.size && input58[zeros].toInt() == 0) zeros++

        val decoded = ByteArray(input.length)
        var outputStart = decoded.size
        var startAt = zeros
        while (startAt < input58.size) {
            val mod = divmod(input58, startAt, BASE, BYTE_BASE)
            if (input58[startAt].toInt() == 0) startAt++
            decoded[--outputStart] = mod
        }

        while (outputStart < decoded.size && decoded[outputStart].toInt() == 0) outputStart++
        return decoded.copyOfRange(outputStart - zeros, decoded.size)
    }

    private fun divmod(
        number: ByteArray,
        firstDigit: Int,
        base: Int,
        divisor: Int,
    ): Byte {
        var remainder = 0
        for (i in firstDigit until number.size) {
            val digit = number[i].toInt() and BYTE_MASK
            val temp = remainder * base + digit
            number[i] = (temp / divisor).toByte()
            remainder = temp % divisor
        }
        return remainder.toByte()
    }
}
