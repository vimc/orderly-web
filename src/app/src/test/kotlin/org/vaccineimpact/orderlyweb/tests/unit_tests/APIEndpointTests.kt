package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.mockito.ArgumentCaptor
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.pac4j.sparkjava.SecurityFilter
import org.pac4j.core.config.Config
import spark.Filter

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
    fun `adds correct security filter if secure, both authentication flags are false`()
    {
        val mockSpark = mock<SparkWrapper>()
        val sut = APIEndpoint(urlFragment = "/test", actionName = "test", controller = TestController::class,
                contentType = ContentTypes.binarydata, secure = true,
                requiredPermissions = listOf(),
                spark = mockSpark)

        sut.additionalSetup("/test")

        val securityFilterArg : ArgumentCaptor<SecurityFilter> = ArgumentCaptor.forClass(SecurityFilter::class.java)
        verify(mockSpark).before(eq("/test"), capture(securityFilterArg))
        val securityFilter = securityFilterArg.value

    }

    /*@Test
    fun `does not add security filter if not secure`()
    {

    }*/
}