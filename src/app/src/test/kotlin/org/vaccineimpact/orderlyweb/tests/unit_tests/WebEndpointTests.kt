package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.config.Config
import org.pac4j.sparkjava.SecurityFilter
import spark.route.HttpMethod
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.SparkWrapper
import org.vaccineimpact.orderlyweb.WebEndpoint
import org.vaccineimpact.orderlyweb.secure
import org.vaccineimpact.orderlyweb.post
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.APISecurityConfigFactory
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class WebEndpointTests: TeamcityTests()
{

    private class TestController(actionContext: ActionContext) : Controller(actionContext)

    @Test
    fun `adds security filter if secure, external auth`()
    {
        val mockSpark = mock<SparkWrapper>()
        val mockAuthorizer = mock<Authorizer<CommonProfile>>()
        val mockConfig = mock<Config> {
            on { authorizers } doReturn mapOf("dummyAuthorizer" to mockAuthorizer)
        }
        val mockConfigFactory = mock<APISecurityConfigFactory> {
           on { build() } doReturn(mockConfig)
        }

        val requiredPermission=  PermissionRequirement.parse("*/testperm")
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                                secure = true, requiredPermissions = listOf(requiredPermission),
                                externalAuth = true,
                                spark = mockSpark, configFactory = mockConfigFactory)

        sut.additionalSetup("/test")

        //Verify all expected methods were called
        verify(mockConfigFactory).build()

        //verify the security filter has been created as expected
        val securityFilterArg: ArgumentCaptor<SecurityFilter> = ArgumentCaptor.forClass(SecurityFilter::class.java)
        verify(mockSpark).before(eq("/test"), capture(securityFilterArg))
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

        field = securityFilterClass.getDeclaredField("authorizers")
        field.isAccessible = true
        val authorizers = field.get(securityFilter)
        assertThat(authorizers).isEqualTo("dummyAuthorizer")

        field = securityFilterClass.getDeclaredField("matchers")
        field.isAccessible = true
        val matchers = field.get(securityFilter)
        assertThat(matchers).isEqualTo("SkipOptions")

    }

    @Test
    fun `adds security filter if secure, not external auth`()
    {

        val mockSpark = mock<SparkWrapper>()
        val mockConfig = mock<Config> {
            on { authorizers } doReturn mapOf()
        }
        val mockConfigFactory = mock<APISecurityConfigFactory> {
            on { build() } doReturn(mockConfig)
        }

        val requiredPermission=  PermissionRequirement.parse("*/testperm")
        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                secure = true, requiredPermissions = listOf(requiredPermission),
                spark = mockSpark, configFactory = mockConfigFactory)

        sut.additionalSetup("/test")

        //Verify all expected methods were called
        verify(mockConfigFactory).build()

        //verify the security filter has been created as expected
        val securityFilterArg: ArgumentCaptor<SecurityFilter> = ArgumentCaptor.forClass(SecurityFilter::class.java)
        verify(mockSpark).before(eq("/test"), capture(securityFilterArg))
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
            on { build() } doReturn(mockConfig)
        }

        val sut = WebEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                spark = mockSpark, configFactory = mockConfigFactory)
        sut.additionalSetup("/test")

        //verify that config factory and spark were not called
        verify(mockConfigFactory, times(0)).build()
        verify(mockSpark, times(0)).before(anyString(), any())
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
}