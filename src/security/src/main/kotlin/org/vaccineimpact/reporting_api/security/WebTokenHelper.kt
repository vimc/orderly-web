package org.vaccineimpact.reporting_api.security

import org.vaccineimpact.reporting_api.db.AppConfig
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey

class WebTokenHelper(keyPair: KeyPair,
                     val issuerName: String)
{
    val issuer = TokenIssuer(keyPair, issuerName)
    val verifier = TokenVerifier(keyPair.public as RSAPublicKey, issuerName)

    companion object
    {
        val oneTimeTokenHelper = WebTokenHelper(KeyHelper.generateKeyPair(),  AppConfig()["onetime_token.issuer"])
    }
}