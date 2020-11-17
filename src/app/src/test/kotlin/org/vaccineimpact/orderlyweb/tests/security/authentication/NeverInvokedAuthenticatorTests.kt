package org.vaccineimpact.orderlyweb.tests.security.authentication

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.security.clients.NeverInvokedAuthenticator
import java.lang.UnsupportedOperationException

class NeverInvokedAuthenticatorTests
{
    @Test
    fun `throws exception when invoked`()
    {
        val sut = NeverInvokedAuthenticator()

        Assertions.assertThatThrownBy { sut.validate(mock(), mock()) }.isInstanceOf(UnsupportedOperationException::class.java)
    }
}