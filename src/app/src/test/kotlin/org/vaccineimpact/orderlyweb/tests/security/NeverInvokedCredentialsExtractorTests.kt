package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.security.clients.NeverInvokedCredentialsExtractor
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.lang.UnsupportedOperationException

class NeverInvokedCredentialsExtractorTestsTests: TeamcityTests()
{
    @Test
    fun `throws exception when invoked`()
    {
        val sut = NeverInvokedCredentialsExtractor()

        Assertions.assertThatThrownBy { sut.extract(mock()) }.isInstanceOf(UnsupportedOperationException::class.java)
    }
}