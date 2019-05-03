package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.mockito.ArgumentCaptor
import org.pac4j.core.authorization.authorizer.Authorizer
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.pac4j.sparkjava.SecurityFilter
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.security.APISecurityConfigFactory
import spark.Filter
import spark.route.HttpMethod

class APIEndpointTests: TeamcityTests()
{
    private class TestController(actionContext: ActionContext) : Controller(actionContext)

    @Test
    fun `adds headers filter if content type is json`()
    {
        val mockSpark = mock<SparkWrapper>()
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                    contentType = ContentTypes.json, spark = mockSpark)

        sut.additionalSetup("/test")

        val filterArg : ArgumentCaptor<Filter> = ArgumentCaptor.forClass(Filter::class.java)
        verify(mockSpark).after(eq("/test"), eq(ContentTypes.json), capture(filterArg))
        val filter = filterArg.value
        assertThat(filter is DefaultHeadersFilter).isTrue()
        assertThat((filter as DefaultHeadersFilter).contentType).isEqualTo("application/json; charset=utf-8")
    }

    @Test
    fun `does not add headers filter if content type is not json`()
    {
        val mockSpark = mock<SparkWrapper>()
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                contentType = ContentTypes.binarydata, spark = mockSpark)

        sut.additionalSetup("/test")

        verify(mockSpark, times(0)).after(any(), any(), any())
    }

    @Test
    fun `adds security filter if secure, both authentication flags are false`()
    {
        val mockSpark = mock<SparkWrapper>()
        val mockAuthorizer = mock<Authorizer<CommonProfile>>()
        val mockConfig = mock<Config> {
            on { authorizers } doReturn mapOf("dummyAuthorizer" to mockAuthorizer)
        }
        val mockConfigFactory = mock<APISecurityConfigFactory> {
            on { allClients() } doReturn("testclients")
            on { build() } doReturn(mockConfig)
        }

        whenever(mockConfigFactory.setRequiredPermissions(any())).doReturn(mockConfigFactory)
        whenever(mockConfigFactory.allowParameterAuthentication()).doReturn(mockConfigFactory)
        whenever(mockConfigFactory.externalAuthentication()).doReturn(mockConfigFactory)

        val permissionRequirement = PermissionRequirement.parse("*/testperm")
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                contentType = ContentTypes.binarydata, secure = true,
                requiredPermissions = listOf(permissionRequirement),
                spark = mockSpark,
                configFactory = mockConfigFactory)

        sut.additionalSetup("/test")

        //Verify all expected methods were called or not called
        verify(mockConfigFactory).setRequiredPermissions(check {
            assertThat(it.size).isEqualTo(1)
            assertThat(it.first()).isEqualTo(permissionRequirement)
        })

        verify(mockConfigFactory).build()
        verify(mockConfigFactory).allClients()

        val securityFilterArg: ArgumentCaptor<SecurityFilter> = ArgumentCaptor.forClass(SecurityFilter::class.java)
        verify(mockSpark).before(eq("/test"), capture(securityFilterArg))

        //verify the security filter has been created as expected
        val securityFilterClass = SecurityFilter::class.java
        val securityFilter = securityFilterArg.value

        var field = securityFilterClass.getDeclaredField("config")
        field.isAccessible = true
        val config = field.get(securityFilter)
        assertThat(config).isEqualTo(mockConfig)

        field = securityFilterClass.getDeclaredField("clients")
        field.isAccessible = true
        val clients = field.get(securityFilter)
        assertThat(clients).isEqualTo("testclients")

        val authorizers = securityFilter.authorizers
        Assertions.assertThat(authorizers).isEqualTo("dummyAuthorizer")

        val matchers = securityFilter.matchers
        Assertions.assertThat(matchers).isEqualTo("SkipOptions")

        //Should not have called these methods
        verify(mockConfigFactory, times(0)).allowParameterAuthentication()
        verify(mockConfigFactory, times(0)).externalAuthentication()
    }

    @Test
    fun `adds security filter if secure, both authentication flags are true`()
    {
        val mockSpark = mock<SparkWrapper>()
        val mockConfig = mock<Config> {
            on { authorizers } doReturn mapOf()
        }
        val mockConfigFactory = mock<APISecurityConfigFactory> {
            on { allClients() } doReturn("")
            on { build() } doReturn(mockConfig)
        }

        whenever(mockConfigFactory.setRequiredPermissions(any())).doReturn(mockConfigFactory)
        whenever(mockConfigFactory.allowParameterAuthentication()).doReturn(mockConfigFactory)
        whenever(mockConfigFactory.externalAuthentication()).doReturn(mockConfigFactory)

        val permissionRequirement = PermissionRequirement.parse("*/testperm")
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                contentType = ContentTypes.binarydata, secure = true,
                requiredPermissions = listOf(permissionRequirement),
                allowParameterAuthentication = true,
                authenticateWithExternalProvider = true,
                spark = mockSpark,
                configFactory = mockConfigFactory)

        sut.additionalSetup("/test")

        //Verify all expectedt methods were called or not called
        verify(mockConfigFactory).setRequiredPermissions(check {
            assertThat(it.size).isEqualTo(1)
            assertThat(it.first()).isEqualTo(permissionRequirement)
        })

        verify(mockConfigFactory).build()
        verify(mockConfigFactory).allClients()

        val securityFilterArg: ArgumentCaptor<SecurityFilter> = ArgumentCaptor.forClass(SecurityFilter::class.java)
        verify(mockSpark).before(eq("/test"), capture(securityFilterArg))

        //verify the security filter has been created with mockConfig
        val securityFilterClass = SecurityFilter::class.java
        val field = securityFilterClass.getDeclaredField("config")
        field.isAccessible = true
        val config = field.get(securityFilterArg.value)
        assertThat(config).isEqualTo(mockConfig)

        verify(mockConfigFactory).allowParameterAuthentication()
        verify(mockConfigFactory).externalAuthentication()
    }

    @Test
    fun `does not add security filter if not secure`()
    {
        val set = setOf("help")
        val aclass = set::class.java

        val mockSpark = mock<SparkWrapper>()
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                contentType = ContentTypes.json, secure=false, spark = mockSpark)

        sut.additionalSetup("/test")

        verify(mockSpark, times(0)).before(any(), any())
    }

    @Test
    fun `can set allowParameterAuthentication`()
    {
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        assertThat(sut.allowParameterAuthentication).isFalse()

        val result = sut.allowParameterAuthentication()
        assertThat(result.allowParameterAuthentication).isTrue()
    }

    @Test
    fun `can secure`()
    {
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        assertThat(sut.secure).isFalse()
        assertThat(sut.requiredPermissions.count()).isEqualTo(0)

        val result = sut.secure(setOf("*/testperm"))

        assertThat(result.secure).isTrue()
        assertThat(result.requiredPermissions.count()).isEqualTo(1)
        assertThat(result.requiredPermissions.first().name).isEqualTo("testperm")
        assertThat(result.requiredPermissions.first().scopeRequirement.toString()).isEqualTo("*")
    }

    @Test
    fun `can set transform`()
    {
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        assertThat(sut.transform).isFalse()

        val result = sut.transform()
        assertThat(result.transform).isTrue()
    }

    @Test
    fun `can set json content type`()
    {
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        assertThat(sut.contentType).isEqualTo("application/octet-stream")

        val result = sut.json()
        assertThat(result.contentType).isEqualTo("application/json")
    }

    @Test
    fun `can set html content type`()
    {
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        val result = sut.html()
        assertThat(result.contentType).isEqualTo("text/html")
    }

    @Test
    fun `can set external authentication`()
    {
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        assertThat(sut.authenticateWithExternalProvider).isFalse()

        val result = sut.externalAuth()
        assertThat(result.authenticateWithExternalProvider).isTrue()
    }

    @Test
    fun `can set post`()
    {
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class)

        assertThat(sut.method).isEqualTo(HttpMethod.get)

        val result = sut.post()
        assertThat(result.method).isEqualTo(HttpMethod.post)
    }
}