package org.vaccineimpact.orderlyweb.tests.security.clients

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.pac4j.jwt.config.signature.AbstractSignatureConfiguration
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebBearerTokenAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.TokenVerifier
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import org.vaccineimpact.orderlyweb.security.clients.JWTCookieClient

class JWTCookieClientTests
{
    private val mockTokenVerifier = mock<TokenVerifier> {
        on { it.expectedIssuer } doReturn "testIssuer"
        on { it.signatureConfiguration } doReturn mock<AbstractSignatureConfiguration>()
    }

    @Test
    fun `initialises as expected`()
    {
        val sut = JWTCookieClient(mockTokenVerifier)

        val authenticator = sut.authenticator
        assertThat(authenticator is OrderlyWebBearerTokenAuthenticator).isTrue()

        sut.init()
        val authorizationGenerator = sut.authorizationGenerators.first()
        assertThat(authorizationGenerator is OrderlyAuthorizationGenerator).isTrue()
    }

    @Test
    fun `can get errorInfo`()
    {
        val sut = JWTCookieClient(mockTokenVerifier)
        val errorInfo = sut.errorInfo
        assertThat(errorInfo.code).isEqualTo("cookie-bearer-token-invalid")
        assertThat(errorInfo.message).isEqualTo("Bearer token not supplied in cookie 'orderlyweb_jwt_token', or bearer token was invalid")
    }

}