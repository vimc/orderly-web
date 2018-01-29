package org.vaccineimpact.reporting_api.tests.security

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.reporting_api.security.MontaguAuthorizer
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests

class MontaguAuthorizerTests : MontaguTests()
{
    @Test
    fun `is not authorized if url claim does not match request`()
    {
        val sut = MontaguAuthorizer(setOf())

        val profile = CommonProfile()
        profile.addAttribute("url", "some/url")

        val fakeContext = mock<SparkWebContext>() {
            on(it.path) doReturn "/fake/url/"
        }

        val result = sut.isAuthorized(fakeContext, listOf(profile))

        assertThat(result).isFalse()

    }

    @Test
    fun `is authorized if claim is global *`()
    {
        val sut = MontaguAuthorizer(setOf())

        val profile = CommonProfile()
        profile.addAttribute("url", "*")

        val fakeContext = mock<SparkWebContext>() {
            on(it.path) doReturn "/fake/url/"
        }

        val result = sut.isAuthorized(fakeContext, listOf(profile))

        assertThat(result).isTrue()

    }
}