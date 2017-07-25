package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.errors.InvalidOneTimeLinkToken
import org.vaccineimpact.reporting_api.security.TokenIssuer
import org.vaccineimpact.reporting_api.security.WebTokenHelper

interface Controller {

}

fun verifyToken(token: String): Map<String, Any> {
    // By checking the database first, we ensure the token is
    // removed from the database, even if it fails some later check
//        if (!repo.validateOneTimeToken(token))
//        {
//            throw InvalidOneTimeLinkToken("used", "Token has already been used (or never existed)")
//        }

    val claims = try {
        WebTokenHelper.oneTimeTokenVerifier.verify(token)
    } catch (e: Exception) {
        // logger.warn("An error occurred validating the onetime link token: $e")
        throw InvalidOneTimeLinkToken("verification", "Unable to verify token; it may be badly formatted or signed with the wrong key")
    }
    if (claims["sub"] != TokenIssuer.oneTimeActionSubject) {
        throw InvalidOneTimeLinkToken("subject", "Expected 'sub' claim to be ${TokenIssuer.oneTimeActionSubject}")
    }
    return claims
}