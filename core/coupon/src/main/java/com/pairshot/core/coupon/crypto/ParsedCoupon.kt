package com.pairshot.core.coupon.crypto

data class ParsedCoupon(
    val couponId: String,
    val durationDays: Long?,
    val issuedAtEpochSeconds: Long,
    val typeToken: String,
    val payloadBytes: ByteArray,
    val signatureBytes: ByteArray,
    val signatureBase58: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParsedCoupon) return false
        return couponId == other.couponId &&
            durationDays == other.durationDays &&
            issuedAtEpochSeconds == other.issuedAtEpochSeconds &&
            typeToken == other.typeToken &&
            payloadBytes.contentEquals(other.payloadBytes) &&
            signatureBytes.contentEquals(other.signatureBytes) &&
            signatureBase58 == other.signatureBase58
    }

    override fun hashCode(): Int {
        var result = couponId.hashCode()
        result = 31 * result + (durationDays?.hashCode() ?: 0)
        result = 31 * result + issuedAtEpochSeconds.hashCode()
        result = 31 * result + typeToken.hashCode()
        result = 31 * result + payloadBytes.contentHashCode()
        result = 31 * result + signatureBytes.contentHashCode()
        result = 31 * result + signatureBase58.hashCode()
        return result
    }
}
