package org.vaccineimpact.reporting_api.tests.unit_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.reporting_api.DirectActionContext
import spark.Request

class DirectActionContextTests
{

    @Test
    fun `can deserialise request body`()
    {
        val request = mock<Request>(){

            on {it.body()} doReturn "{ \"some\" : \"value\" }"
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
        val request = mock<Request>(){

            on {it.body()} doReturn ""
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