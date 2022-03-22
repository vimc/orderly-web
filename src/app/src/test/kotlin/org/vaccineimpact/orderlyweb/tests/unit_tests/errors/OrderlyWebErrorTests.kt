package org.vaccineimpact.orderlyweb.tests.unit_tests.errors

import com.google.gson.JsonSyntaxException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.errors.*
import org.vaccineimpact.orderlyweb.models.ResultStatus

class OrderlyWebErrorTests
{

    @Test
    fun `can create InvalidOneTimeLinkToken`()
    {
        val sut = InvalidOneTimeLinkToken("testCode", "testMessage")
        assertExpectedOrderlyWebError(sut, 400,
                "testMessage", "invalid-token-testCode")
    }

    @Test
    fun `can create MissingParameterError`()
    {
        val sut = MissingParameterError("testParam")
        assertExpectedOrderlyWebError(sut, 400,
                "Missing parameter 'testParam'", "bad-request")
    }

    @Test
    fun `can create UnableToConnectToDatabaseError`()
    {
        val sut = UnableToConnectToDatabaseError("https://test")

        assertExpectedOrderlyWebError(sut, 500,
                "Unable to establish connection to the database at https://test",
                "database-connection-error")
    }

    @Test
    fun `can create UnableToParseJsonError`()
    {
        val sut = UnableToParseJsonError(JsonSyntaxException("com.google.gson.stream.MalformedJsonException: {\"dodgy\"}"))

        assertExpectedOrderlyWebError(sut, 400,
                "Unable to parse supplied JSON: {\"dodgy\"}",
                "bad-json")
    }

    @Test
    fun `can create UnknownObjectError`()
    {
        val sut = UnknownObjectError("123", org.vaccineimpact.orderlyweb.models.Report::class)

        assertExpectedOrderlyWebError(sut, 404,
                "Unknown report : '123'", "unknown-report")
    }

    private fun assertExpectedOrderlyWebError(sut: OrderlyWebError, expectedHttpStatus: Int, expectedMessage: String,
                                              expectedResultCode: String)
    {
        assertThat(sut.httpStatus).isEqualTo(expectedHttpStatus)
        assertThat(sut.message).isEqualTo(
                "the following problems occurred:\n" + expectedMessage)

        val result = sut.asResult()
        assertThat(result.status).isEqualTo(ResultStatus.FAILURE)
        assertThat(result.errors.count()).isEqualTo(1)
        assertThat(result.errors[0].code).isEqualTo(expectedResultCode)
    }
}
