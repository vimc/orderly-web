package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import spark.Request

class DirectActionContextTests: TeamcityTests()
{

    @Test
    fun `can deserialise request body`()
    {
        val request = mock<Request>() {

            on { it.body() } doReturn "{ \"some\" : \"value\" }"
        }

        val context = mock<SparkWebContext>() {
            on {
                it.sparkRequest
            } doReturn request

        }

        val sut = DirectActionContext(context)

        val result = sut.postData()
        assert(result["some"].equals("value"))
    }

    @Test
    fun `returns empty map if no data`()
    {
        val request = mock<Request>() {

            on { it.body() } doReturn ""
        }

        val context = mock<SparkWebContext>() {
            on {
                it.sparkRequest
            } doReturn request

        }

        val sut = DirectActionContext(context)

        val result = sut.postData()
        assert(result.equals(emptyMap<String, String>()))
    }
}