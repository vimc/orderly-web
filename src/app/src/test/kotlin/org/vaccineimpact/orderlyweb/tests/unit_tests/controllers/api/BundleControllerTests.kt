package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.check
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol.HTTP_1_1
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.api.BundleController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.MissingParameterError

class BundleControllerTests : ControllerTest() {

    private val config = mock<Config> {
        on { get("orderly.server") } doReturn "http://example.com"
        // on { this.authorizationEnabled } doReturn true
    }

    @Test
    fun `packs a report`() {
        val context = mock<ActionContext> {
            on { params(":name") } doReturn "report1"
            on { postData<String>() } doReturn mapOf(
                "a" to "b"
            )
            on { getSparkResponse() } doReturn mockSparkResponse
            // on { this.permissions } doReturn PermissionSet()
        }
        val httpClient = getHttpClient(context, "/v1/bundle/pack/" + context.params(":name"), ByteArray(0))
        val controller = BundleController(context, config, httpClient)

        assertThat(controller.pack()).isTrue()
        verify(httpClient).newCall(
            check {
                assertThat(it.body!!.contentType().toString()).isEqualTo("application/json; charset=utf-8")
                assertThat(it.body!!.contentLength()).isEqualTo(9) // {"a": "b"}
            }
        )
    }

    @Test
    fun `packs a report fails if name not provided`() {
        val context = mock<ActionContext> {
            on { getSparkResponse() } doReturn mockSparkResponse
            // on { this.permissions } doReturn PermissionSet()
        }
        val httpClient = getHttpClient(context, "/v1/bundle/pack/" + context.params(":name"), ByteArray(0))
        val controller = BundleController(context, config, httpClient)

        assertThatThrownBy { controller.pack() }.isInstanceOf(MissingParameterError::class.java)
    }

    @Test
    fun `imports a report`() {
        val context = mock<ActionContext> {
            on { getRequestBodyAsBytes() } doReturn ByteArray(0)
            // on { this.permissions } doReturn PermissionSet()
        }
        val body = "true"
        val httpClient = getHttpClient(context, "/v1/bundle/import", body.toByteArray())
        val controller = BundleController(context, config, httpClient)
        assertThat(controller.import()).isEqualTo(body)
    }

    private fun getHttpClient(context: ActionContext, path: String, body: ByteArray): OkHttpClient {
        val request = Request.Builder()
            .url(config["orderly.server"] + path)
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody())
            .build()
        val call = mock<Call>() {
            on { execute() } doReturn response
        }
        val httpClient = mock<OkHttpClient>() {
            on { newCall(any<Request>()) } doReturn call
        }
        return httpClient
    }
}
