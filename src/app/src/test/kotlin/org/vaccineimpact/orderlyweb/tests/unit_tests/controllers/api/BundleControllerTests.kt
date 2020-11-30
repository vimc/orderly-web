package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol.HTTP_1_1
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.controllers.api.BundleController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError

class BundleControllerTests : ControllerTest()
{

    private val config = mock<Config> {
        on { get("orderly.server") } doReturn "http://example.com"
    }

    @Test
    fun `packs a report`()
    {
        val context = mock<ActionContext> {
            on { params(":name") } doReturn "report1"
            on { postData<String>() } doReturn mapOf(
                    "a" to "b"
            )
            on { getSparkResponse() } doReturn mockSparkResponse
        }
        val httpClient = getHttpClient("/v1/bundle/pack/${context.params(":name")}")
        val controller = BundleController(context, config, httpClient)

        assertThat(controller.pack()).isTrue()
        verify(httpClient).newCall(
                check {
                    assertThat(it.body!!.contentType().toString()).isEqualTo("application/json; charset=utf-8")
                    assertThat(it.body!!.contentLength()).isEqualTo(Gson().toJson(context.postData<String>()).length.toLong())
                }
        )
        verify(servletResponse).contentType = "application/zip"
    }

    @Test
    fun `packs a report passes through instance`()
    {
        val context = mock<ActionContext> {
            on { params(":name") } doReturn "report1"
            on { queryString() } doReturn "instance=foo"
            on { getSparkResponse() } doReturn mockSparkResponse
        }
        val httpClient = getHttpClient("/v1/bundle/pack/${context.params(":name")}")
        val controller = BundleController(context, config, httpClient)

        assertThat(controller.pack()).isTrue()
        verify(httpClient).newCall(
                check {
                    assertThat(it.url.query).isEqualTo("instance=foo")
                }
        )
    }

    @Test
    fun `packs a report fails on orderly server error`()
    {
        val context = mock<ActionContext> {
            on { params(":name") } doReturn "report1"
        }
        val httpClient = getHttpClient("/v1/bundle/pack/${context.params(":name")}", responseCode = 500, responseMessage = "Internal Server Error")
        val controller = BundleController(context, config, httpClient)

        assertThatThrownBy { controller.pack() }.isInstanceOf(OrderlyServerError::class.java)
    }

    @Test
    fun `packs a report fails if name not provided`()
    {
        val request = mock<spark.Request>()
        val context = mock<SparkWebContext> {
            on { sparkRequest } doReturn request
        }
        val httpClient = getHttpClient("/v1/bundle/pack/foo")
        val controller = BundleController(DirectActionContext(context, config), config, httpClient)

        assertThatThrownBy { controller.pack() }.isInstanceOf(MissingParameterError::class.java)
    }

    @Test
    fun `imports a report`()
    {
        val context = mock<ActionContext> {
            on { getRequestBodyAsBytes() } doReturn ByteArray(0)
        }
        val body = """{"status":"success","errors":null,"data":true}"""
        val httpClient = getHttpClient("/v1/bundle/import", body.toByteArray())
        val controller = BundleController(context, config, httpClient)
        assertThat(controller.import()).isEqualTo(body)
    }

    @Test
    fun `imports a report fails on orderly server error`()
    {
        val context = mock<ActionContext> {
            on { getRequestBodyAsBytes() } doReturn ByteArray(0)
        }
        val httpClient = getHttpClient("/v1/bundle/import", responseCode = 500, responseMessage = "Internal Server Error")
        val controller = BundleController(context, config, httpClient)

        assertThatThrownBy { controller.import() }.isInstanceOf(OrderlyServerError::class.java)
    }

    private fun getHttpClient(path: String, responseBody: ByteArray = ByteArray(0), responseCode: Int = 200, responseMessage: String = "OK"): OkHttpClient
    {
        val request = Request.Builder()
                .url(config["orderly.server"] + path)
                .build()
        val response = Response.Builder()
                .request(request)
                .protocol(HTTP_1_1)
                .code(responseCode)
                .message(responseMessage)
                .body(responseBody.toResponseBody())
                .build()
        val call = mock<Call> {
            on { execute() } doReturn response
        }
        return mock {
            on { newCall(any()) } doReturn call
        }
    }
}
