package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.security.clients.NeverInvokedCredentialsExtractor
import java.lang.UnsupportedOperationException

class NeverInvokedCredentialsExtractorTestsTests
{
    @Test
    fun `throws exception when invoked`()
    {
        val sut = NeverInvokedCredentialsExtractor()

        Assertions.assertThatThrownBy { sut.extract(mock(), mock()) }.isInstanceOf(UnsupportedOperationException::class.java)
    }
}