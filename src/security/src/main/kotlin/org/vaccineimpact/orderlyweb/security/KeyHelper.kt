package org.vaccineimpact.orderlyweb.security

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.*
import java.util.*

object KeyHelper
{
    private val logger: Logger = LoggerFactory.getLogger(KeyHelper::class.java)

    val keyPair = generateKeyPair()

    fun generateKeyPair(): KeyPair
    {
        logger.info("Generating a new RSA keypair for token signing.")
        logger.info("If other applications need to verify tokens they should use the following public key:")
        val generator = KeyPairGenerator.getInstance("RSA").apply {
            initialize(1024)
        }
        val keypair = generator.generateKeyPair()
        val publicKey = Base64.getEncoder().encode(keypair.public.encoded)
        logger.info("Public key for token verification: $publicKey")
        return keypair
    }
}