package org.vaccineimpact.reporting_api.tests.unit_tests.controllers

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import spark.Response
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse

abstract class ControllerTest: MontaguTests()
{
     protected val mockOutputStream = mock<ServletOutputStream>()

     protected val servletResponse = mock<HttpServletResponse>(){
         on {this.outputStream} doReturn mockOutputStream
         on {this.contentType} doReturn ""
     }

     protected val mockSparkResponse = mock<Response>(){
         on {this.raw()} doReturn servletResponse
     }

}