package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.sparkjava.SecurityFilter
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.APISecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationProvider
import spark.Filter
import spark.route.HttpMethod

class WebEndpointTests
{
    private class TestController(actionContext: ActionContext) : Controller(actionContext)

    @Test
    fun `adds headers filter if content type is json`()
    {
        val mockSpark = mock<SparkWrapper>()
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                contentType = ContentTypes.json, spark = mockSpark)

        sut.additionalSetup("/test")

        val filterArg: ArgumentCaptor<Filter> = ArgumentCaptor.forClass(Filter::class.java)
        verify(mockSpark).after(eq("/test"), eq(ContentTypes.json), capture(filterArg))
        val filter = filterArg.value
        assertThat(filter is DefaultHeadersFilter).isTrue()
        assertThat((filter as DefaultHeadersFilter).contentType).isEqualTo("application/json; charset=utf-8")
    }

    @Test
    fun `does not add headers filter if content type is not json`()
    {
        val mockSpark = mock<SparkWrapper>()
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                contentType = ContentTypes.binarydata, spark = mockSpark)

        sut.additionalSetup("/test")

        verify(mockSpark, times(0)).after(any(), any(), any())
    }

    @Test
    fun `adds security filter if secure, external auth`()
    {
        val mockSpark = mock<SparkWrapper>()
        val mockAuthorizer = mock<Authorizer<CommonProfile>>()
        val mockConfig = mock<Config> {
            on { authorizers } doReturn mapOf("dummyAuthorizer" to mockAuthorizer)
        }
        val mockConfigFactory = mock<APISecurityConfigFactory> {
            on { build() } doReturn (mockConfig)
        }

        val requiredPermission = PermissionRequirement.parse("*/testperm")
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                secure = true, requiredPermissions = listOf(requiredPermission),
                externalAuth = true,
                spark = mockSpark, configFactory = mockConfigFactory)

        sut.additionalSetup("/test")

        // Verify all expected methods were called
        verify(mockConfigFactory).build()

        // verify the security filter has been created as expected
        val securityFilterArg: ArgumentCaptor<SecurityFilter> = ArgumentCaptor.forClass(SecurityFilter::class.java)
        verify(mockSpark).before(eq("/test"), eq("text/html"), eq(HttpMethod.get), capture(securityFilterArg))
        val securityFilter = securityFilterArg.value

        val securityFilterClass = SecurityFilter::class.java
        var field = securityFilterClass.getDeclaredField("config")
        field.isAccessible = true
        val config = field.get(securityFilter)
        assertThat(config).isEqualTo(mockConfig)

        field = securityFilterClass.getDeclaredField("clients")
        field.isAccessible = true
        val clients = field.get(securityFilter)
        assertThat(clients).isEqualTo("MontaguIndirectClient")

        val authorizers = securityFilter.authorizers
        assertThat(authorizers).isEqualTo("dummyAuthorizer")

        val matchers = securityFilter.matchers
        assertThat(matchers).isEqualTo("SkipOptions")
    }

    @Test
    fun `adds security filter if secure, not external auth, with Montagu auth`()
    {

        val mockSpark = mock<SparkWrapper>()
        val mockConfig = mock<Config> {
            on { authorizers } doReturn mapOf()
        }
        val mockConfigFactory = mock<APISecurityConfigFactory> {
            on { build() } doReturn (mockConfig)
        }

        val requiredPermission = PermissionRequirement.parse("*/testperm")
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                secure = true, requiredPermissions = listOf(requiredPermission),
                spark = mockSpark, configFactory = mockConfigFactory)

        sut.additionalSetup("/test")

        // Verify all expected methods were called
        verify(mockConfigFactory).build()

        // verify the security filter has been created as expected
        val securityFilterArg: ArgumentCaptor<SecurityFilter> = ArgumentCaptor.forClass(SecurityFilter::class.java)
        verify(mockSpark).before(eq("/test"), eq("text/html"), eq(HttpMethod.get), capture(securityFilterArg))
        val securityFilter = securityFilterArg.value

        val securityFilterClass = SecurityFilter::class.java
        var field = securityFilterClass.getDeclaredField("config")
        field.isAccessible = true
        val config = field.get(securityFilter)
        assertThat(config).isEqualTo(mockConfig)

        field = securityFilterClass.getDeclaredField("clients")
        field.isAccessible = true
        val clients = field.get(securityFilter)
        assertThat(clients).isEqualTo("MontaguIndirectClient")
    }

    @Test
    fun `adds security filter if secure, not external auth, with Github auth`()
    {

        val mockSpark = mock<SparkWrapper>()
        val mockConfig = mock<Config> {
            on { authorizers } doReturn mapOf()
        }
        val mockConfigFactory = mock<APISecurityConfigFactory> {
            on { build() } doReturn (mockConfig)
        }

        val mockAuthConfig = mock<AuthenticationConfig> {
            on { getConfiguredProvider() } doReturn AuthenticationProvider.GitHub
        }

        val requiredPermission = PermissionRequirement.parse("*/testperm")
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                secure = true, requiredPermissions = listOf(requiredPermission),
                spark = mockSpark, configFactory = mockConfigFactory, authenticationConfig = mockAuthConfig)

        sut.additionalSetup("/test")

        // Verify all expected methods were called
        verify(mockConfigFactory).build()

        // verify the security filter has been created as expected
        val securityFilterArg: ArgumentCaptor<SecurityFilter> = ArgumentCaptor.forClass(SecurityFilter::class.java)
        verify(mockSpark).before(eq("/test"), eq("text/html"), eq(HttpMethod.get), capture(securityFilterArg))
        val securityFilter = securityFilterArg.value

        val securityFilterClass = SecurityFilter::class.java
        var field = securityFilterClass.getDeclaredField("config")
        field.isAccessible = true
        val config = field.get(securityFilter)
        assertThat(config).isEqualTo(mockConfig)

        field = securityFilterClass.getDeclaredField("clients")
        field.isAccessible = true
        val clients = field.get(securityFilter)
        assertThat(clients).isEqualTo("OrderlyWebIndirectClient")
    }

    @Test
    fun `does not add security filter if not secure`()
    {
        val mockSpark = mock<SparkWrapper>()
        val mockConfig = mock<Config> {
            on { authorizers } doReturn mapOf()
        }
        val mockConfigFactory = mock<APISecurityConfigFactory> {
            on { build() } doReturn (mockConfig)
        }

        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                spark = mockSpark, configFactory = mockConfigFactory)
        sut.additionalSetup("/test")

        // verify that config factory and spark were not called
        verify(mockConfigFactory, times(0)).build()
        verify(mockSpark, times(0)).before(anyString(), any(), any(), any())
    }

    @Test
    fun `can secure`()
    {
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        assertThat(sut.secure).isFalse()
        assertThat(sut.requiredPermissions.count()).isEqualTo(0)

        val result = sut.secure(setOf("*/testperm"))

        assertThat(result.secure).isTrue()
        assertThat(result.requiredPermissions.count()).isEqualTo(1)
        assertThat(result.requiredPermissions.first().name).isEqualTo("testperm")
        assertThat(result.requiredPermissions.first().scopeRequirement.toString()).isEqualTo("*")
    }

    @Test
    fun `can set post`()
    {
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        assertThat(sut.method).isEqualTo(HttpMethod.get)

        val result = sut.post()
        assertThat(result.method).isEqualTo(HttpMethod.post)
    }

    @Test
    fun `can set transform`()
    {
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        assertThat(sut.transform).isFalse()

        val result = sut.transform()
        assertThat(result.transform).isTrue()
    }
}
