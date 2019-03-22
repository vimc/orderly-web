package org.vaccineimpact.orderlyweb.tests.integration_tests.helpers

import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.security.issuing.KeyHelper
import org.vaccineimpact.orderlyweb.security.InternalUser
import org.vaccineimpact.orderlyweb.security.WebTokenHelper
import java.time.Duration
import java.time.Instant
import java.util.*

class TestTokenGenerator(config: Config = AppConfig())
{
    private val helper = WebTokenHelper(KeyHelper.keyPair, config["token.issuer"])

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
}