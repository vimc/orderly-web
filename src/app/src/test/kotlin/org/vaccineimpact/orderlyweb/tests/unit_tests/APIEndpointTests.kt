package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatcher
import org.mockito.Captor
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.pac4j.sparkjava.SecurityFilter
import org.pac4j.core.config.Config
import org.vaccineimpact.orderlyweb.models.PermissionRequirement
import org.vaccineimpact.orderlyweb.models.ScopeRequirement
import org.vaccineimpact.orderlyweb.security.APISecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.allowParameterAuthentication
import org.vaccineimpact.orderlyweb.security.externalAuthentication
import spark.Filter
import java.util.*

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
        val mockConfig = mock<Config> {
            on { authorizers } doReturn mapOf()
        }
        val mockConfigFactory = mock<APISecurityConfigFactory> {
            on { build() } doReturn mockConfig
            on { allClients } doReturn listOf()
        }

        val permissionRequirement = PermissionRequirement.parse("*/testperm")
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                contentType = ContentTypes.binarydata, secure = true,
                requiredPermissions = listOf(permissionRequirement),
                spark = mockSpark,
                configFactory = mockConfigFactory)

        sut.additionalSetup("/test")

        /*verify(mockConfigFactory).setRequiredPermissions(check {
            assertThat(it.size).isEqualTo(1)
            assertThat(it.first()).isEqualTo(permissionRequirement)
        })*/

        //Should not have called these methods
        verify(mockConfigFactory, times(0)).allowParameterAuthentication()
        verify(mockConfigFactory, times(0)).externalAuthentication()

        verify(mockConfigFactory).build()

        val securityFilterArg: ArgumentCaptor<SecurityFilter> = ArgumentCaptor.forClass(SecurityFilter::class.java)
        verify(mockSpark).before(eq("/test"), capture(securityFilterArg))

        //verify the security filter has been created with mockConfig
        val securityFilterClass = SecurityFilter::class.java
        val field = securityFilterClass.getDeclaredField("config")
        field.isAccessible = true
        val config = field.get(sut)
        assertThat(config).isEqualTo(mockConfig)
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
}