package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.*
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import spark.Request
import spark.Response
import java.nio.charset.Charset
import javax.servlet.MultipartConfigElement
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.Part

class DirectActionContextTests
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

    val mockProfileManager = mock<ProfileManager> {
        on { it.profiles } doReturn listOf(mockUserProfile)
    }

    val mockPostRequest = mock<Request> {
        on { it.body() } doReturn "{ \"some\" : \"value\" }"
        on { it.bodyAsBytes() } doReturn "{ \"some\" : \"value\" }".toByteArray()
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
    fun `can get request body`()
    {
        val sut = DirectActionContext(mockPostSparkContext)
        val result = sut.getRequestBody()
        assert(result.equals("{ \"some\" : \"value\" }"))
    }

    @Test
    fun `can get request body as bytes`()
    {
        val sut = DirectActionContext(mockPostSparkContext)
        val result = sut.getRequestBodyAsBytes()
        assert(String(result).equals("{ \"some\" : \"value\" }"))
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

    @Test
    fun `queryParams constructs map from set of parameters`()
    {
        val request = mock<Request> {
            on { queryParams() } doReturn setOf("a", "c")
            on { queryParams("a") } doReturn "b"
        }
        val context = mock<SparkWebContext> {
            on { sparkRequest } doReturn request
        }

        val sut = DirectActionContext(context)

        assertThat(sut.queryParams()).isEqualTo(mapOf<String, String?>(
            "a" to "b",
            "c" to null
        ))
    }

    @Test
    fun `can get part reader`()
    {
        val mockRequest = getMockRequestForPart()
        val mockContext = mock<SparkWebContext> {
            on { sparkRequest } doReturn mockRequest
        }

        val sut = DirectActionContext(mockContext)
        val result = sut.getPartReader("testPartName")
        result.use {
            assertThat(it.readText()).isEqualTo("MOCK")
        }

        val configElementArg = ArgumentCaptor.forClass(MultipartConfigElement::class.java)
        verify(mockRequest).attribute(eq("org.eclipse.jetty.multipartConfig"), capture(configElementArg))
        assertThat(configElementArg.value.location).isEqualTo("/tmp")
    }

    @Test
    fun `can get part`()
    {
        val mockRequest = getMockRequestForPart()
        val mockContext = mock<SparkWebContext> {
            on { sparkRequest } doReturn mockRequest
        }

        val sut = DirectActionContext(mockContext)
        val result = sut.getPart("testPartName")
        assertThat(result).isEqualTo("MOCK")
    }

    @Test
    fun `getPartReader throws BadRequest when content is empty`()
    {
        val mockRequest = mock<Request>{
            on { contentLength() } doReturn 0
        }
        val mockContext = mock<SparkWebContext> {
            on { sparkRequest } doReturn mockRequest
        }

        val sut = DirectActionContext(mockContext)
        assertThatThrownBy{ sut.getPartReader("file") }
                .isInstanceOf(BadRequest::class.java)
                .hasMessageContaining("No data provided")
    }

    private fun getMockRequestForPart(): Request
    {
        val mockStream = IOUtils.toInputStream("MOCK", Charset.defaultCharset())

        val mockPart = mock<Part> {
            on { inputStream } doReturn mockStream
        }

        val mockRaw = mock<HttpServletRequest> {
            on { getPart("testPartName") } doReturn mockPart
        }

        return mock<Request> {
            on { raw() } doReturn mockRaw
            on { contentLength() } doReturn 10
        }
    }
}
