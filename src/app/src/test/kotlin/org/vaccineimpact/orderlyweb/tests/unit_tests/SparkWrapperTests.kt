package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.SparkServiceWrapper
import org.vaccineimpact.orderlyweb.errors.UnsupportedValueException
import spark.Request
import spark.Response
import spark.ResponseTransformer
import spark.Route
import spark.route.HttpMethod

class JoinPathTests: TeamcityTests()
{
    class TestRoute: Route
    {
        override fun handle(request: Request, response: Response): Any
        {
            return true
        }
    }

    class TestResponseTransformer: ResponseTransformer
    {
        override fun render(model: Any): String
        {
            return model.toString()
        }
    }

    @Test
    fun `can map all Http methods without error`()
    {
        val sut = SparkServiceWrapper()

        sut.map("/get", HttpMethod.get, "*/*", TestRoute(), null)
        sut.map("/post", HttpMethod.post, "*/*", TestRoute(), null)
        sut.map("/put", HttpMethod.put, "*/*", TestRoute(), null)
        sut.map("/patch", HttpMethod.patch, "*/*", TestRoute(), null)
        sut.map("/delete", HttpMethod.delete, "*/*", TestRoute(), null)

        val transformer = TestResponseTransformer()
        sut.map("/get2", HttpMethod.get, "*/*", TestRoute(), transformer)
        sut.map("/post2", HttpMethod.post, "*/*", TestRoute(), transformer)
        sut.map("/put2", HttpMethod.put, "*/*", TestRoute(), transformer)
        sut.map("/patch2", HttpMethod.patch, "*/*", TestRoute(), transformer)
        sut.map("/delete2", HttpMethod.delete, "*/*", TestRoute(), transformer)
    }

    @Test
    fun `throws UnsupportedValueException if map unexpected Http method`()
    {
        val sut = SparkServiceWrapper()
        assertThatThrownBy{ sut.map("/options", HttpMethod.options, "*/*", TestRoute(), null) }
                .isInstanceOf(UnsupportedValueException::class.java)

        val transformer = TestResponseTransformer()
        assertThatThrownBy{ sut.map("/options", HttpMethod.options, "*/*", TestRoute(), transformer) }
                .isInstanceOf(UnsupportedValueException::class.java)
    }
}