package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.SparkServiceWrapper
import org.vaccineimpact.orderlyweb.errors.UnsupportedValueException
import spark.ResponseTransformer
import spark.Route
import spark.route.HttpMethod

class JoinPathTests: TeamcityTests()
{
    private val mockRoute = mock<Route>()
    private val mockTransformer = mock<ResponseTransformer>()

    @Test
    fun `can map all Http methods without error`()
    {
        val sut = SparkServiceWrapper()

        sut.map("/get", HttpMethod.get, "*/*", mockRoute, null)
        sut.map("/post", HttpMethod.post, "*/*", mockRoute, null)
        sut.map("/put", HttpMethod.put, "*/*", mockRoute, null)
        sut.map("/patch", HttpMethod.patch, "*/*", mockRoute, null)
        sut.map("/delete", HttpMethod.delete, "*/*", mockRoute, null)

        sut.map("/get2", HttpMethod.get, "*/*", mockRoute, mockTransformer)
        sut.map("/post2", HttpMethod.post, "*/*", mockRoute, mockTransformer)
        sut.map("/put2", HttpMethod.put, "*/*", mockRoute, mockTransformer)
        sut.map("/patch2", HttpMethod.patch, "*/*", mockRoute, mockTransformer)
        sut.map("/delete2", HttpMethod.delete, "*/*", mockRoute, mockTransformer)
    }

    @Test
    fun `throws UnsupportedValueException if map unexpected Http method`()
    {
        val sut = SparkServiceWrapper()
        assertThatThrownBy{ sut.map("/options", HttpMethod.options, "*/*", mockRoute, null) }
                .isInstanceOf(UnsupportedValueException::class.java)

        assertThatThrownBy{ sut.map("/options", HttpMethod.options, "*/*", mockRoute, mockTransformer) }
                .isInstanceOf(UnsupportedValueException::class.java)
    }
}