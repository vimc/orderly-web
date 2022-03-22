package org.vaccineimpact.orderlyweb.tests.security.authorization

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyWebAPIAuthorizer

class OrderlyWebAPIAuthorizerTests
{
    @Test
    fun `is not authorized if url claim does not match request`()
    {
        val sut = OrderlyWebAPIAuthorizer(setOf())

        val profile = CommonProfile()
        profile.addAttribute("url", "some/url")

        val fakeContext = mock<SparkWebContext>() {
            on(it.path) doReturn "/fake/url/"
        }

        val result = sut.isAuthorized(fakeContext, listOf(profile))

        assertThat(result).isFalse()
    }

    @Test
    fun `is authorized if url claim matches request`()
    {
        val sut = OrderlyWebAPIAuthorizer(setOf())

        val profile = CommonProfile()
        profile.addAttribute("url", "/some/url/")

        val fakeContext = mock<SparkWebContext>() {
            on(it.path) doReturn "/some/url/"
        }

        val result = sut.isAuthorized(fakeContext, listOf(profile))

        assertThat(result).isTrue()
    }

    @Test
    fun `is authorized if url claim matches request and query params match`()
    {
        val sut = OrderlyWebAPIAuthorizer(setOf())

        val profile = CommonProfile()
        profile.addAttribute("url", "/some/url/?query=whatever")

        val fakeContext = mock<SparkWebContext>() {
            on(it.path) doReturn "/some/url/?query=whatever"
        }

        val result = sut.isAuthorized(fakeContext, listOf(profile))

        assertThat(result).isTrue()
    }

    @Test
    fun `is not authorized if request does not contain same query params as claim`()
    {
        val sut = OrderlyWebAPIAuthorizer(setOf())

        val profile = CommonProfile()
        profile.addAttribute("url", "/some/url/?query=whatever")

        val fakeContext = mock<SparkWebContext>() {
            on(it.path) doReturn "/some/url/"
        }

        val result = sut.isAuthorized(fakeContext, listOf(profile))

        assertThat(result).isFalse()
    }

    @Test
    fun `is not authorized if claim does not have same query params as request`()
    {
        val sut = OrderlyWebAPIAuthorizer(setOf())

        val profile = CommonProfile()
        profile.addAttribute("url", "/some/url/")

        val fakeContext = mock<SparkWebContext>() {
            on(it.path) doReturn "/some/url/?query=whatever"
        }

        val result = sut.isAuthorized(fakeContext, listOf(profile))

        assertThat(result).isFalse()
    }

    @Test
    fun `is sensitive to query parameter values`()
    {
        val sut = OrderlyWebAPIAuthorizer(setOf())

        val profile = CommonProfile()
        profile.addAttribute("url", "/some/url/?query=whatever")

        val fakeContext = mock<SparkWebContext>() {
            on(it.path) doReturn "/some/url/?query=somethingelse"
        }

        val result = sut.isAuthorized(fakeContext, listOf(profile))

        assertThat(result).isFalse()
    }

    @Test
    fun `is authorized if claim is global *`()
    {
        val sut = OrderlyWebAPIAuthorizer(setOf())

        val profile = CommonProfile()
        profile.addAttribute("url", "*")

        val fakeContext = mock<SparkWebContext>() {
            on(it.path) doReturn "/fake/url/"
        }

        val result = sut.isAuthorized(fakeContext, listOf(profile))

        assertThat(result).isTrue()
    }
}
