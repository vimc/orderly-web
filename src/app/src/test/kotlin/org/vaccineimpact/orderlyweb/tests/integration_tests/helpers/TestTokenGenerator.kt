package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.security.KeyHelper
import org.vaccineimpact.orderlyweb.security.InternalUser
import org.vaccineimpact.orderlyweb.security.WebTokenHelper
import java.io.File
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Duration
import java.time.Instant
import java.util.*

class TestTokenGenerator(config: Config = AppConfig())
{
    private val keyPair = KeyHelper.generateKeyPair()
    private val helper = WebTokenHelper(keyPair, config["token.issuer"])

    init
    {
        savePublicKey()
    }

    fun generateToken(user: InternalUser): String
    {
        return helper.issuer.generator.generate(claims(user))
    }

    fun claims(user: InternalUser): Map<String, Any>
    {
        return mapOf(
                "iss" to helper.issuerName,
                "sub" to user.username,
                "exp" to Date.from(Instant.now().plus(Duration.ofMinutes(1))),
                "permissions" to user.permissions,
                "roles" to user.roles
        )
    }

    private fun savePublicKey()
    {
        // This takes the generated keypair and puts it where the API will find it
        // so that it can validate tokens

        val publicKey = keyPair.public as RSAPublicKey
        val privateKey = keyPair.private as RSAPrivateKey

        val publicSpec = X509EncodedKeySpec(publicKey.encoded)
        val privateSpec = PKCS8EncodedKeySpec(privateKey.encoded)

        val publicKeyFile = File(KeyHelper.keyPath, "public_key.der")
        publicKeyFile.createNewFile()
        publicKeyFile.writeBytes(publicSpec.encoded)

        val privateKeyFile = File(KeyHelper.keyPath, "private_key.der")
        privateKeyFile.createNewFile()
        privateKeyFile.writeBytes(privateSpec.encoded)
    }
}