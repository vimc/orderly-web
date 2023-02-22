package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.pac4j.core.config.Config
import org.pac4j.sparkjava.CallbackRoute
import org.pac4j.sparkjava.LogoutRoute
import org.vaccineimpact.orderlyweb.APIEndpoint
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.SparkWrapper
import org.vaccineimpact.orderlyweb.app_start.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import spark.TemplateEngine

class RouterTests
{
    private val mockTemplateEngine = mock<TemplateEngine>()
    private val mockActionResolver = ActionResolver(mockTemplateEngine)
    private val mockSparkWrapper = mock<SparkWrapper>()
    private val mockAuthRouteBuilder = mock<AuthenticationRouteBuilder>() {
        on { logout() } doReturn LogoutRoute(Config())
        on { loginCallback() } doReturn CallbackRoute(Config())
    }
    private val mockErrorHandler = ErrorHandler(mockTemplateEngine)
    private val mockAuthConfig = mock<AuthenticationConfig>() {
        on { useAuth } doReturn true
    }

    object TestRouteBuilder : RouteBuilder
    {
        override fun getEndpoints(useAuth: Boolean): List<EndpointDefinition>
        {
            if (useAuth)
            {
                return listOf(
                        APIEndpoint("/first/url", Controller::class, "action", transform = true),
                        APIEndpoint("/second/url/", Controller::class, "action", transform = false)
                )
            }
            else return listOf()
        }
    }

    @Test
    fun `maps endpoints with and without trailing slashes`()
    {
        val sut = Router(mockActionResolver, mockAuthRouteBuilder, mockSparkWrapper, mockErrorHandler, mockAuthConfig)

        val urls = sut.mapEndpoints(TestRouteBuilder, "urlBase").toMutableList()
        urls.addAll(sut.mapEndpoints(TestRouteBuilder, "anotherBase"))

        val expected = listOf(
                "urlBase/first/url/",
                "urlBase/second/url/",
                "anotherBase/first/url/",
                "anotherBase/second/url/"
        )

        assertThat(urls).hasSameElementsAs(expected)

        expected.forEach {
            verify(mockSparkWrapper).map(eq(it), any(), any(), any(), anyOrNull())
            verify(mockSparkWrapper).map(eq(it.dropLast(1)), any(), any(), any(), anyOrNull())
        }
    }

    @Test
    fun `passes through useAuth argument`()
    {
        val mockAuthConfig = mock<AuthenticationConfig>() {
            on { useAuth } doReturn false
        }

        val sut = Router(mockActionResolver, mockAuthRouteBuilder, mockSparkWrapper, mockErrorHandler, mockAuthConfig)
        val urls = sut.mapEndpoints(TestRouteBuilder, "urlBase").toMutableList()
        urls.addAll(sut.mapEndpoints(TestRouteBuilder, "anotherBase"))
        assertThat(urls).isEmpty()

    }

    @Test
    fun `only adds response transformer if endpoint should be transformed`()
    {
        val sut = Router(mockActionResolver, mockAuthRouteBuilder, mockSparkWrapper, mockErrorHandler, mockAuthConfig)

        sut.mapEndpoints(TestRouteBuilder, "urlBase")
        val transformedUrl = "urlBase/first/url/"
        val untransformedUrl = "urlBase/second/url/"

        verify(mockSparkWrapper).map(eq(transformedUrl), any(), any(), any(), notNull())
        verify(mockSparkWrapper).map(eq(untransformedUrl), any(), any(), any(), isNull())
    }

    @Test
    fun `maps logout route with and without trailing slash`()
    {
        Router(mockActionResolver, mockAuthRouteBuilder, mockSparkWrapper, mockErrorHandler, mockAuthConfig)

        verify(mockSparkWrapper).mapGet(eq("logout"), any())
        verify(mockSparkWrapper).mapGet(eq("logout/"), any())
    }

    @Test
    fun `maps login route with and without trailing slash`()
    {
        Router(mockActionResolver, mockAuthRouteBuilder, mockSparkWrapper, mockErrorHandler, mockAuthConfig)

        verify(mockSparkWrapper).mapGet(eq("login"), any())
        verify(mockSparkWrapper).mapGet(eq("login/"), any())
    }

}