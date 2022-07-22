package org.vaccineimpact.orderlyweb.security.issuing

import java.security.KeyPair
import java.security.KeyPairGenerator

object KeyHelper
{
    val keyPair = generateKeyPair()
    const val keySize = 2048

    fun generateKeyPair(): KeyPair
    {
        val generator = KeyPairGenerator.getInstance("RSA").apply {
            initialize(keySize)
        }
        return generator.generateKeyPair()
    }
}
