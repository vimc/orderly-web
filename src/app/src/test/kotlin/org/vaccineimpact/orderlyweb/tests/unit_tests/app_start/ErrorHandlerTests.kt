package org.vaccineimpact.orderlyweb.tests.unit_tests.app_start

import com.google.gson.JsonSyntaxException
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.vaccineimpact.orderlyweb.app_start.ErrorHandler
import org.vaccineimpact.orderlyweb.app_start.ResponseErrorHandler
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import org.vaccineimpact.orderlyweb.errors.UnableToParseJsonError
import org.vaccineimpact.orderlyweb.errors.UnexpectedError
import spark.Request
import spark.Response
import java.lang.reflect.InvocationTargetException

class ErrorHandlerTests
{
    private val mockAPIHandler = mock<ResponseErrorHandler>()
    private val mockWebHandler = mock<ResponseErrorHandler>()

    private val mockError = mock<OrderlyWebError>()
    private val mockResponse = mock<Response>()

    private val mockApiRequest = mock<Request> {
        on { this.pathInfo() } doReturn "/api/v1/test/"
        on { this.headers("Accept") } doReturn "anything"
    }

    @Test
    fun `handleError invokes apiResponseErrorHandler for api path url`()
    {
        val sut = ErrorHandler(mock(), mockWebHandler, mockAPIHandler)

        sut.handleError(mockError, mockApiRequest, mockResponse)

        verify(mockAPIHandler).handleError(mockError, mockApiRequest, mockResponse)
        verify(mockWebHandler, never()).handleError(mockError, mockApiRequest, mockResponse)
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

    @Test
    fun `handleInvocationError handles OrderlyWebError`()
    {
        val sut = ErrorHandler(mock(), mockWebHandler, mockAPIHandler)

        val mockInvocationError = mock<InvocationTargetException> {
            on { cause } doReturn mockError
        }

        sut.handleInvocationError(mockInvocationError, mockApiRequest, mockResponse)

        verify(mockAPIHandler).handleError(mockError, mockApiRequest, mockResponse)
    }

    @Test
    fun `handleInvocationError handles JsonSyntaxException`()
    {
        val sut = ErrorHandler(mock(), mockWebHandler, mockAPIHandler)
        val exception = JsonSyntaxException("test json exception")

        val mockInvocationError = mock<InvocationTargetException> {
            on { cause } doReturn exception
        }

        sut.handleInvocationError(mockInvocationError, mockApiRequest, mockResponse)

        val errorCaptor: ArgumentCaptor<OrderlyWebError> = ArgumentCaptor.forClass(OrderlyWebError::class.java)
        verify(mockAPIHandler).handleError(capture(errorCaptor), eq(mockApiRequest), eq(mockResponse))

        val error = errorCaptor.value
        assertThat(error).isInstanceOf(UnableToParseJsonError::class.java)
    }

    @Test
    fun `handleInvocationError handles unexpected errors`()
    {
        val sut = ErrorHandler(mock(), mockWebHandler, mockAPIHandler)
        val exception = Exception("any exception")

        val mockInvocationError = mock<InvocationTargetException> {
            on { cause } doReturn exception
        }

        sut.handleInvocationError(mockInvocationError, mockApiRequest, mockResponse)

        val errorCaptor: ArgumentCaptor<OrderlyWebError> = ArgumentCaptor.forClass(OrderlyWebError::class.java)
        verify(mockAPIHandler).handleError(capture(errorCaptor), eq(mockApiRequest), eq(mockResponse))

        val error = errorCaptor.value
        assertThat(error).isInstanceOf(UnexpectedError::class.java)
    }
}
