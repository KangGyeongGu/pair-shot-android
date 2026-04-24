package com.pairshot.core.coupon.crypto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.security.SecureRandom

@OptIn(ExperimentalSerializationApi::class)
class CouponCryptoTest {
    private lateinit var privateKey: Ed25519PrivateKeyParameters
    private lateinit var publicKey: Ed25519PublicKeyParameters
    private lateinit var verifier: CouponSignatureVerifier
    private lateinit var parser: CouponPayloadParser

    @Before
    fun setUp() {
        val generator = Ed25519KeyPairGenerator()
        generator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val keyPair = generator.generateKeyPair()
        privateKey = keyPair.private as Ed25519PrivateKeyParameters
        publicKey = keyPair.public as Ed25519PublicKeyParameters
        val publicBytes = publicKey.encoded
        verifier = CouponSignatureVerifier(publicKeyLoader = { publicBytes })
        parser = CouponPayloadParser()
    }

    @Test
    fun `parse and verify valid 30 day coupon succeeds`() {
        val code = generateCouponCode(durationDays = 30L)
        val parsed = parser.parse(code).getOrThrow()
        assertEquals(30L, parsed.durationDays)
        assertEquals("30D", parsed.typeToken)
        assertTrue(verifier.verify(parsed.payloadBytes, parsed.signatureBytes))
    }

    @Test
    fun `parse and verify valid unlimited coupon succeeds`() {
        val code = generateCouponCode(durationDays = null)
        val parsed = parser.parse(code).getOrThrow()
        assertEquals(null, parsed.durationDays)
        assertEquals("INF", parsed.typeToken)
        assertTrue(verifier.verify(parsed.payloadBytes, parsed.signatureBytes))
    }

    @Test
    fun `parse and verify valid custom 120 day coupon succeeds`() {
        val code = generateCouponCode(durationDays = 120L)
        val parsed = parser.parse(code).getOrThrow()
        assertEquals(120L, parsed.durationDays)
        assertEquals("120D", parsed.typeToken)
        assertTrue(verifier.verify(parsed.payloadBytes, parsed.signatureBytes))
    }

    @Test
    fun `forged signature with wrong key fails verification`() {
        val payload = buildPayloadBytes(couponId = "abc", durationDays = 30L, iat = 1_700_000_000L)
        val otherGenerator = Ed25519KeyPairGenerator()
        otherGenerator.init(Ed25519KeyGenerationParameters(SecureRandom()))
        val otherPair = otherGenerator.generateKeyPair()
        val otherPrivate = otherPair.private as Ed25519PrivateKeyParameters
        val forgedSignature = sign(otherPrivate, payload)
        assertFalse(verifier.verify(payload, forgedSignature))
    }

    @Test
    fun `tampered payload breaks verification`() {
        val code = generateCouponCode(durationDays = 30L)
        val parsed = parser.parse(code).getOrThrow()
        val tamperedPayload = parsed.payloadBytes.copyOf()
        tamperedPayload[0] = (tamperedPayload[0].toInt() xor 0x01).toByte()
        assertFalse(verifier.verify(tamperedPayload, parsed.signatureBytes))
    }

    @Test
    fun `type mismatch between prefix and payload is rejected`() {
        val payload = buildPayloadBytes(couponId = "abc", durationDays = 30L, iat = 1_700_000_000L)
        val signature = sign(privateKey, payload)
        val combined = payload + signature
        val forgedCode = "PS-7D-" + Base58.encode(combined)
        val result = parser.parse(forgedCode)
        assertTrue(result.isFailure)
        val error = (result.exceptionOrNull() as CouponParseException).reason
        assertEquals(CouponParseError.TypeMismatch, error)
    }

    @Test
    fun `malformed prefix is rejected`() {
        val result = parser.parse("FOO-30D-bad")
        assertTrue(result.isFailure)
        assertEquals(
            CouponParseError.InvalidFormat,
            (result.exceptionOrNull() as CouponParseException).reason,
        )
    }

    @Test
    fun `invalid base58 content is rejected`() {
        val result = parser.parse("PS-30D-!!!")
        assertTrue(result.isFailure)
        assertEquals(
            CouponParseError.InvalidFormat,
            (result.exceptionOrNull() as CouponParseException).reason,
        )
    }

    private fun generateCouponCode(durationDays: Long?): String {
        val payload =
            buildPayloadBytes(
                couponId = "c7f8a2e1-6b3d-4e8a-9c1f-2d5b7e9a0c3d",
                durationDays = durationDays,
                iat = 1_700_000_000L,
            )
        val signature = sign(privateKey, payload)
        val combined = payload + signature
        val prefix = if (durationDays == null) "INF" else "${durationDays}D"
        return "PS-$prefix-" + Base58.encode(combined)
    }

    private fun buildPayloadBytes(
        couponId: String,
        durationDays: Long?,
        iat: Long,
    ): ByteArray =
        Cbor.encodeToByteArray(
            CouponPayload.serializer(),
            CouponPayload(
                couponId = couponId,
                durationDays = durationDays,
                issuedAtEpochSeconds = iat,
            ),
        )

    private fun sign(
        privateKey: Ed25519PrivateKeyParameters,
        payload: ByteArray,
    ): ByteArray {
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(payload, 0, payload.size)
        return signer.generateSignature()
    }
}
