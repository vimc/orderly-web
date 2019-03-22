package org.vaccineimpact.orderlyweb.security

import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.security.authentication.TokenVerifier
import org.vaccineimpact.orderlyweb.security.issuing.KeyHelper
import org.vaccineimpact.orderlyweb.security.issuing.TokenIssuer
import java.security.KeyPair

class WebTokenHelper(keyPair: KeyPair,
                     val issuerName: String)
{
    val issuer = TokenIssuer(keyPair, issuerName)
    val verifier = TokenVerifier(keyPair, issuerName)

    companion object
    {
        val instance = WebTokenHelper(KeyHelper.keyPair, AppConfig()["token.issuer"])
    }
}