package org.vaccineimpact.reporting_api.security

import org.slf4j.LoggerFactory
import java.io.File
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

object KeyHelper {
    private val keyFactory = KeyFactory.getInstance("RSA")
    val keyDir = "/etc/montagu/reports_api/token_key"
    val keyFile = File(keyDir, "public_key.der")
    private val logger = LoggerFactory.getLogger(KeyHelper::class.java)

    val authPublicKey by lazy { loadPublicKey() }

    private fun loadPublicKey(): RSAPublicKey {
        val keyBytes = keyFile.readBytes()
        val spec = X509EncodedKeySpec(keyBytes)
        val publicKey = keyFactory.generatePublic(spec) as RSAPublicKey
        logger.info("Using this public key for auth token verification: " + Base64.getEncoder().encodeToString(publicKey.encoded))
        return publicKey
    }

    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA").apply {
            initialize(1024)
        }
        return generator.generateKeyPair()
    }
}