package org.vaccineimpact.orderlyweb.tests.unit_tests.app_start

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.vaccineimpact.orderlyweb.app_start.ErrorHandler
import org.vaccineimpact.orderlyweb.app_start.ResponseErrorHandler
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import spark.Request
import spark.Response

class ErrorHandlerTests : TeamcityTests()
{
    private val mockAPIHandler = mock<ResponseErrorHandler>()
    private val mockWebHandler = mock<ResponseErrorHandler>()

    private val mockError = mock<OrderlyWebError>()
    private val mockResponse = mock<Response>()

    @Test
    fun `handleError invokes apiResponseErrorHandler for api path url`()
    {
        val sut = ErrorHandler(mock(), mockWebHandler, mockAPIHandler)

        val mockRequest = mock<Request> {
            on { this.pathInfo() } doReturn "/api/v1/test/"
            on { this.headers("Accept") } doReturn "anything"
        }

        sut.handleError(mockError, mockRequest, mockResponse)

        verify(mockAPIHandler).handleError(mockError, mockRequest, mockResponse)
        verify(mockWebHandler, never()).handleError(mockError, mockRequest, mockResponse)
    }

    @Test
    fun `handleError invokes apiResponseErrorHandler for web path url accepting json`()
    {
        val sut = ErrorHandler(mock(), mockWebHandler, mockAPIHandler)

        val mockRequest = mock<Request> {
            on { this.pathInfo() } doReturn "/test/"
            on { this.headers("Accept") } doReturn "application/json, text/plain"
        }

        sut.handleError(mockError, mockRequest, mockResponse)

        verify(mockAPIHandler).handleError(mockError, mockRequest, mockResponse)
        verify(mockWebHandler, never()).handleError(mockError, mockRequest, mockResponse)
    }

    @Test
    fun `handleError invokes webResponseErrorHandler for web path url not accepting json`()
    {
        val sut = ErrorHandler(mock(), mockWebHandler, mockAPIHandler)

        val mockRequest = mock<Request> {
            on { this.pathInfo() } doReturn "/test/"
            on { this.headers("Accept") } doReturn "text/html,application/xhtml+xml"
        }

        sut.handleError(mockError, mockRequest, mockResponse)

        verify(mockWebHandler).handleError(mockError, mockRequest, mockResponse)
        verify(mockAPIHandler, never()).handleError(mockError, mockRequest, mockResponse)
    }
}