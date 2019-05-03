package org.vaccineimpact.orderlyweb.tests.security.authentication

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.security.clients.NeverInvokedAuthenticator
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.lang.UnsupportedOperationException

class NeverInvokedAuthenticatorTests: TeamcityTests()
{
    @Test
    fun `throws exception when invoked`()
    {
        val sut = NeverInvokedAuthenticator()

        Assertions.assertThatThrownBy { sut.validate(mock(), mock()) }.isInstanceOf(UnsupportedOperationException::class.java)
    }
}