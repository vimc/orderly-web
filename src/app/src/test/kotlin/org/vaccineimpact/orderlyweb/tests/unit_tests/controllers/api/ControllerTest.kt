package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import spark.Response
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse

abstract class ControllerTest
{
    protected val mockOutputStream = mock<ServletOutputStream>()

    protected val servletResponse = mock<HttpServletResponse>() {
        on { this.outputStream } doReturn mockOutputStream
        on { this.contentType } doReturn ""
    }

    protected val mockSparkResponse = mock<Response>() {
        on { this.raw() } doReturn servletResponse
    }

}