package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Protocol.HTTP_1_1
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.api.BundleController
import org.vaccineimpact.orderlyweb.db.Config

class BundleControllerTests : ControllerTest() {

    @Test
    fun `packs a report`() {
        val context = mock<ActionContext> {
            on { params(":name") } doReturn "report1"
            on { getSparkResponse() } doReturn mockSparkResponse
            // on { this.permissions } doReturn PermissionSet()
        }

        val config = mock<Config> {
            on { get("orderly.server") } doReturn "http://example.com"
            // on { this.authorizationEnabled } doReturn true
        }

        val request = Request.Builder()
            .url(config["orderly.server"] + "/v1/bundle/pack/" + context.params(":name"))
            .build()
        val response = Response.Builder()
            .request(request)
            .protocol(HTTP_1_1)
            .code(200)
            .message("OK")
            .body(ByteArray(0).toResponseBody())
            .build()
        val call = mock<Call>() {
            on { execute() } doReturn response
        }
        val httpClient = mock<OkHttpClient>() {
            on { newCall(any()) } doReturn call // argWhere?
        }

        val result = BundleController(context, config, httpClient).pack()

        assertThat(result).isTrue() // com.nhaarman.mockito_kotlin.verify?
    }
}
