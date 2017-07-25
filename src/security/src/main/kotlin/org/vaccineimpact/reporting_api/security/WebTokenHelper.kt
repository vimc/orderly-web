package org.vaccineimpact.reporting_api.security

import org.vaccineimpact.reporting_api.db.Config
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey

class WebTokenHelper(keyPair: KeyPair, val issuerName: String)
{
    val issuer = TokenIssuer(keyPair, issuerName)
    val verifier = TokenVerifier(keyPair.public as RSAPublicKey, issuerName)

    companion object{
        private val oneTimeTokenHelper = WebTokenHelper(KeyHelper.generateKeyPair(), Config["onetime_token.issuer"])
        val oneTimeTokenIssuer = oneTimeTokenHelper.issuer
        val oneTimeTokenVerifier = oneTimeTokenHelper.verifier
    }
}