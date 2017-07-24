package org.vaccineimpact.reporting_api.security

import org.pac4j.http.client.direct.HeaderClient

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTHeaderClient(helper: TokenVerifier)
    : HeaderClient(
        "Authorization",
        "Bearer ",
        MontaguTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer))