package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import okhttp3.Headers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.PorcelainAPI
import org.vaccineimpact.orderlyweb.PorcelainResponse
import org.vaccineimpact.orderlyweb.controllers.api.OutpackController
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest
import spark.RequestResponseFactory
import spark.Response
import java.io.OutputStream.nullOutputStream
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse

class OutpackControllerTests : ControllerTest()
{
    @Test
    fun `gets correct route from outpack with no splat`()
    {
        val mockOutputStream = mock<ServletOutputStream>()
        val rawResponse = mock<HttpServletResponse>() {
            on { it.outputStream } doReturn mockOutputStream
        }
        val mockContext = mock<ActionContext> {
            on { it.getSparkResponse() } doReturn RequestResponseFactory.create(rawResponse)
        }
        val mockOutpack = mock<PorcelainAPI> {
            on { it.get("/", mockContext) } doReturn PorcelainResponse("index", 200, Headers.headersOf())
        }

        val sut = OutpackController(mockContext, mockOutpack)
        sut.get()

        verify(mockOutputStream).write("index".toByteArray())
    }

    @Test
    fun `gets correct route from outpack with splat`()
    {
        val mockOutputStream = mock<ServletOutputStream>()
        val rawResponse = mock<HttpServletResponse>() {
            on { it.outputStream } doReturn mockOutputStream
        }
        val mockContext = mock<ActionContext>() {
            on { it.getSparkResponse() } doReturn RequestResponseFactory.create(rawResponse)
            on { it.splat() } doReturn arrayOf("some/route")
        }
        val mockOutpack = mock<PorcelainAPI> {
            on { it.get("/some/route", mockContext) } doReturn PorcelainResponse("route", 200, Headers.headersOf())
        }

        val sut = OutpackController(mockContext, mockOutpack)
        sut.get()

        verify(mockOutputStream).write("route".toByteArray())
    }
}