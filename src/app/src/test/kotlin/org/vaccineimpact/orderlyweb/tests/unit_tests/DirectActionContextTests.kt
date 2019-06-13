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
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import spark.Request
import spark.Response
import javax.servlet.http.HttpServletResponse

class DirectActionContextTests: TeamcityTests()
{
    val mockUserProfile = mock<CommonProfile> {
        on { it.getAttributes() } doReturn mapOf(
                "orderlyWebPermissions" to
                        PermissionSet(
                                setOf(
                                        ReifiedPermission("testPermission1", Scope.Global()),
                                        ReifiedPermission("testPermission2", Scope.Specific("testPrefix", "testId"))
                                )
                        )
        )
    }

    val mockProfileManager = mock<ProfileManager<CommonProfile>> {
        on { it.getAll(true) } doReturn listOf(mockUserProfile)
    }

    @Test
    fun `can deserialise request body`()
    {
        val request = mock<Request> {

            on { it.body() } doReturn "{ \"some\" : \"value\" }"
        }

        val context = mock<SparkWebContext> {
            on {
                it.sparkRequest
            } doReturn request

        }

        val sut = DirectActionContext(context)

        val result = sut.postData()
        assert(result["some"].equals("value"))
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

        val result = sut.postData()
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
        val hasPerm = sut.hasPermission(ReifiedPermission("testPermission2", Scope.Specific("testPrefix", "testId")))

        assertThat(hasPerm).isTrue()
    }

    @Test
    fun `hasPermission is false if permission is not in user profile`()
    {
        val context = mock<SparkWebContext>()

        val sut = DirectActionContext(context, profileManager = mockProfileManager)
        val hasPerm = sut.hasPermission(ReifiedPermission("testPermission2", Scope.Global()))

        assertThat(hasPerm).isFalse()
    }

    @Test
    fun `requirePermission throws MissingRequiredPermissionError if user does not have permission`()
    {
        val context = mock<SparkWebContext>()

        val sut = DirectActionContext(context, profileManager = mockProfileManager)
        assertThatThrownBy { sut.requirePermission(ReifiedPermission("testPermission2", Scope.Global())) }
                .isInstanceOf(MissingRequiredPermissionError::class.java)

    }

    @Test
    fun `requirePermission does not throw exception if user has permission`()
    {
        val context = mock<SparkWebContext>()

        val sut = DirectActionContext(context, profileManager = mockProfileManager)
        sut.requirePermission(ReifiedPermission("testPermission2", Scope.Specific("testPrefix", "testId")))
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
}