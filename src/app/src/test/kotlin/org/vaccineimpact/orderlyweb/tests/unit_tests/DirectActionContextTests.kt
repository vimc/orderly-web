package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions.*
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import spark.Request
import spark.Response
import javax.servlet.http.HttpServletResponse

class DirectActionContextTests : TeamcityTests()
{
    val mockUserProfile = mock<CommonProfile> {
        on { it.getAttributes() } doReturn mapOf(
                "orderlyWebPermissions" to
                        PermissionSet(
                                setOf(
                                        ReifiedPermission("reports.read", Scope.Global()),
                                        ReifiedPermission("reports.read", Scope.Specific("version", "testId")),
                                        ReifiedPermission("reports.read", Scope.Specific("report", "testname"))
                                )
                        )
        )
    }

    val mockProfileManager = mock<ProfileManager<CommonProfile>> {
        on { it.getAll(true) } doReturn listOf(mockUserProfile)
    }

    val mockPostRequest = mock<Request> {
        on { it.body() } doReturn "{ \"some\" : \"value\" }"
    }

    val mockPostSparkContext = mock<SparkWebContext> {
        on {
            it.sparkRequest
        } doReturn mockPostRequest

    }

    @Test
    fun `can deserialise request body`()
    {
        val sut = DirectActionContext(mockPostSparkContext)

        val result = sut.postData<String>()
        assert(result["some"].equals("value"))
    }

    @Test
    fun `can get value from request body`()
    {
        val sut = DirectActionContext(mockPostSparkContext)

        val result = sut.postData<String>("some")
        assert(result.equals("value"))
    }

    @Test
    fun `MissingParameterError thrown if get nonexistent value from request body`()
    {
        val sut = DirectActionContext(mockPostSparkContext)

        assertThatThrownBy{ sut.postData("something else") }.isInstanceOf(MissingParameterError::class.java)
    }

    @Test
    fun `returns empty map if no data`()
    {
        val request = mock<Request> {

            on { it.body() } doReturn ""
        }

        val context = mock<SparkWebContext> {
            on {
                it.sparkRequest
            } doReturn request

        }

        val sut = DirectActionContext(context)

        val result = sut.postData<String>()
        assert(result.equals(emptyMap<String, String>()))
    }

    @Test
    fun `can add default response headers`()
    {
        val rawResponse = mock<HttpServletResponse> {
            on { it.containsHeader("Content-Encoding") } doReturn false
        }

        val response = mock<Response> {
            on { it.raw() } doReturn rawResponse
        }

        val context = mock<SparkWebContext> {
            on {
                it.sparkResponse
            } doReturn response

        }

        val sut = DirectActionContext(context)
        sut.addDefaultResponseHeaders("testContentType")

        verify(rawResponse).setContentType("testContentType")
        verify(rawResponse).addHeader("Content-Encoding", "gzip")
        verify(rawResponse).addHeader("Access-Control-Allow-Credentials", "true")
    }

    @Test
    fun `hasPermission is true if authorization is not enabled`()
    {
        val context = mock<SparkWebContext>()
        val appConfig = mock<Config> {
            on { it.authorizationEnabled } doReturn false
        }

        val sut = DirectActionContext(context, appConfig)
        val hasPerm = sut.hasPermission(ReifiedPermission("a fake permission", Scope.Global()))

        assertThat(hasPerm).isTrue()
    }

    @Test
    fun `hasPermission is true if permission is in user profile`()
    {
        val context = mock<SparkWebContext>()

        val sut = DirectActionContext(context, profileManager = mockProfileManager)
        val hasPerm = sut.hasPermission(ReifiedPermission("reports.read", Scope.Specific("version", "testId")))

        assertThat(hasPerm).isTrue()
    }

    @Test
    fun `hasPermission is false if permission is not in user profile`()
    {
        val context = mock<SparkWebContext>()

        val sut = DirectActionContext(context, profileManager = mockProfileManager)
        val hasPerm = sut.hasPermission(ReifiedPermission("users.manage", Scope.Global()))

        assertThat(hasPerm).isFalse()
    }

    @Test
    fun `requirePermission throws MissingRequiredPermissionError if user does not have permission`()
    {
        val context = mock<SparkWebContext>()

        val sut = DirectActionContext(context, profileManager = mockProfileManager)
        assertThatThrownBy { sut.requirePermission(ReifiedPermission("users.manage", Scope.Global())) }
                .isInstanceOf(MissingRequiredPermissionError::class.java)

    }

    @Test
    fun `requirePermission does not throw exception if user has permission`()
    {
        val context = mock<SparkWebContext>()

        val sut = DirectActionContext(context, profileManager = mockProfileManager)
        sut.requirePermission(ReifiedPermission("reports.read", Scope.Specific("version", "testId")))
    }

    @Test
    fun `getSparkResponse returns context response`()
    {
        val mockResponse = mock<Response>()

        val context = mock<SparkWebContext> {
            on {
                it.sparkResponse
            } doReturn mockResponse

        }

        val sut = DirectActionContext(context)
        val response = sut.getSparkResponse()

        assertThat(response).isSameAs(mockResponse)
    }

    @Test
    fun `setStatusCode sets code on response`()
    {
        val response = mock<Response>()

        val context = mock<SparkWebContext> {
            on {
                it.sparkResponse
            } doReturn response
        }

        val sut = DirectActionContext(context)
        sut.setStatusCode(500)

        verify(response).status(500)
    }

    @Test
    fun `isGlobalReader is true if fine grained auth is turned off`()
    {
        val context = mock<SparkWebContext>()
        val mockConfig = mock<Config> {
            on { authorizationEnabled } doReturn false
        }
        val sut = DirectActionContext(context, profileManager = mockProfileManager,
                appConfig = mockConfig)

        assertThat(sut.isGlobalReader()).isTrue()
    }

    @Test
    fun `isReviewer is true if fine grained auth is turned off`()
    {
        val context = mock<SparkWebContext>()
        val mockConfig = mock<Config> {
            on { authorizationEnabled } doReturn false
        }
        val sut = DirectActionContext(context, profileManager = mockProfileManager,
                appConfig = mockConfig)

        assertThat(sut.isReviewer()).isTrue()
    }

    @Test
    fun `report reading scopes are derived from permission`()
    {
        val context = mock<SparkWebContext>()
        val sut = DirectActionContext(context, profileManager = mockProfileManager)

        assertThat(sut.reportReadingScopes).hasSameElementsAs(listOf("testname"))
    }
}