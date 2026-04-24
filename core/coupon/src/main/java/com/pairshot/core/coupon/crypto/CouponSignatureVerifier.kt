package com.pairshot.core.coupon.crypto

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponSignatureVerifier(
    private val publicKeyLoader: () -> ByteArray,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this(publicKeyLoader = { loadPublicKeyFromAssets(context) })

    private val publicKeyParameters: Ed25519PublicKeyParameters by lazy {
        val keyBytes = publicKeyLoader()
        require(keyBytes.size == ED25519_PUBLIC_KEY_SIZE) {
            "Ed25519 public key must be $ED25519_PUBLIC_KEY_SIZE bytes, got ${keyBytes.size}"
        }
        Ed25519PublicKeyParameters(keyBytes, 0)
    }

    fun verify(
        payload: ByteArray,
        signature: ByteArray,
    ): Boolean {
        if (signature.size != ED25519_SIGNATURE_SIZE) return false
        val signer = Ed25519Signer()
        signer.init(false, publicKeyParameters)
        signer.update(payload, 0, payload.size)
        return signer.verifySignature(signature)
    }

    companion object {
        private const val PUBLIC_KEY_ASSET = "coupon-public-raw.b64"
        const val ED25519_PUBLIC_KEY_SIZE = 32
        const val ED25519_SIGNATURE_SIZE = 64

        private fun loadPublicKeyFromAssets(context: Context): ByteArray {
            val rawBase64 =
                context.assets
                    .open(PUBLIC_KEY_ASSET)
                    .bufferedReader()
                    .use { it.readText() }
                    .trim()
            return Base64.decode(rawBase64, Base64.DEFAULT)
        }
    }
}
