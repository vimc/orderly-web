package org.vaccineimpact.orderlyweb.security.issuing

import java.security.KeyPair
import java.security.KeyPairGenerator

object KeyHelper
{
    val keyPair = generateKeyPair()

    fun generateKeyPair(): KeyPair
    {
        val generator = KeyPairGenerator.getInstance("RSA").apply {
            initialize(1024)
        }
        return generator.generateKeyPair()
    }
}