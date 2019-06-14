package org.vaccineimpact.orderlyweb.tests.security.clients

import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import org.vaccineimpact.orderlyweb.security.authentication.TokenVerifier
import org.vaccineimpact.orderlyweb.security.clients.JWTCookieClient
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class JWTCookieClientTests : TeamcityTests()
{
    @Test
    fun `initialises as expected`()
    {
        val sut = JWTCookieClient(mock())
    }

    @Test
    fun `can get errorInfo`()
    {

    }

}