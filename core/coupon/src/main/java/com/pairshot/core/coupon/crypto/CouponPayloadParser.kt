package com.pairshot.core.coupon.crypto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import javax.inject.Inject

@OptIn(ExperimentalSerializationApi::class)
class CouponPayloadParser
    @Inject
    constructor() {
        fun parse(code: String): Result<ParsedCoupon> = runCatching { parseOrThrow(code.trim()) }

        private fun parseOrThrow(normalized: String): ParsedCoupon {
            requireFormat(normalized.startsWith(PREFIX))
            val body = normalized.removePrefix(PREFIX)
            val firstDash = body.indexOf('-')
            requireFormat(firstDash > 0 && firstDash < body.length - 1)
            val typeToken = body.substring(0, firstDash)
            val base58 = body.substring(firstDash + 1)
            val decoded = decodeBase58OrFail(base58)
            requireFormat(decoded.size > SIGNATURE_SIZE)
            val payloadBytes = decoded.copyOfRange(0, decoded.size - SIGNATURE_SIZE)
            val signatureBytes = decoded.copyOfRange(decoded.size - SIGNATURE_SIZE, decoded.size)
            val payload = decodePayloadOrFail(payloadBytes)
            val typeExpected = expectedTypeToken(payload.durationDays)
            if (typeToken != typeExpected) {
                throw CouponParseException(CouponParseError.TypeMismatch)
            }
            return ParsedCoupon(
                couponId = payload.couponId,
                durationDays = payload.durationDays,
                issuedAtEpochSeconds = payload.issuedAtEpochSeconds,
                typeToken = typeToken,
                payloadBytes = payloadBytes,
                signatureBytes = signatureBytes,
                signatureBase58 = Base58.encode(signatureBytes),
            )
        }

        private fun decodeBase58OrFail(base58: String): ByteArray =
            runCatching { Base58.decode(base58) }
                .getOrElse { throw CouponParseException(CouponParseError.InvalidFormat) }

        private fun decodePayloadOrFail(payloadBytes: ByteArray): CouponPayload =
            runCatching { Cbor.decodeFromByteArray(CouponPayload.serializer(), payloadBytes) }
                .getOrElse { throw CouponParseException(CouponParseError.InvalidFormat) }

        private fun requireFormat(predicate: Boolean) {
            if (!predicate) throw CouponParseException(CouponParseError.InvalidFormat)
        }

        private fun expectedTypeToken(days: Long?): String =
            when {
                days == null -> UNLIMITED_TOKEN
                days > 0 -> "${days}D"
                else -> ""
            }

        private companion object {
            const val PREFIX = "PS-"
            const val SIGNATURE_SIZE = 64
            const val UNLIMITED_TOKEN = "INF"
        }
    }

class CouponParseException(
    val reason: CouponParseError,
) : IllegalArgumentException(reason::class.simpleName)
