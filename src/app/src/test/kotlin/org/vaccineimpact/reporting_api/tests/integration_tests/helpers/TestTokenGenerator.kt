package org.vaccineimpact.reporting_api.tests.integration_tests.helpers

import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.security.KeyHelper
import org.vaccineimpact.reporting_api.security.MontaguUser
import org.vaccineimpact.reporting_api.security.WebTokenHelper
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.time.Duration
import java.time.Instant
import java.util.*

class TestTokenGenerator
{
    private val keyPair = KeyHelper.generateKeyPair()
    private val helper = WebTokenHelper(keyPair, AppConfig["token.issuer"])
    private val logger = LoggerFactory.getLogger(TestTokenGenerator::class.java)

    init
    {
        savePublicKey()
    }

    fun generateToken(user: MontaguUser): String
    {
        return helper.issuer.generator.generate(claims(user))
    }

    fun claims(user: MontaguUser): Map<String, Any>
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
        // This takes the public key from our test key pair and puts it
        // where the Reports API will read it in. This means that tokens
        // generated by this TestTokenGenerator will be acceptable to the
        // API.
        val publicKey = keyPair.public as RSAPublicKey
        val spec = X509EncodedKeySpec(publicKey.encoded)
        logger.info("API should use this public key for auth token verification: "
                + Base64.getEncoder().encodeToString(publicKey.encoded))
        KeyHelper.keyFile.createNewFile()
        KeyHelper.keyFile.writeBytes(spec.encoded)
    }
}