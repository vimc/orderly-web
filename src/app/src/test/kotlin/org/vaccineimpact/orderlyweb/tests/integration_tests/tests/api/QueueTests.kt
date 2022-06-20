package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import com.fasterxml.jackson.databind.node.ArrayNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class QueueTests : IntegrationTest()
{
    @Test
    fun `can get queue status`()
    {
        apiRequestHelper.post("/reports/minimal/run/",
                mapOf("params" to mapOf<String, String>()),
                userEmail = fakeGlobalReportReviewer())

        val response = apiRequestHelper.get("/queue/status", userEmail = fakeGlobalReportReader())

        assertSuccessful(response)
        JSONValidator.validateAgainstOrderlySchema(response.text, "QueueStatusResponse")
        val tasks = JSONValidator.getData(response.text)["tasks"] as ArrayNode
        assertThat(tasks.count()).isEqualTo(1)
        assertThat(tasks[0]["key"].textValue()).isNotEmpty()
        assertThat(tasks[0]["status"].textValue()).isNotEmpty()
        assertThat(tasks[0]["inputs"]["name"].textValue()).isEqualTo("minimal")
    }

    @Test
    fun `cannot get queue status if not authenticated`()
    {
        val headers = mapOf("Accept" to ContentTypes.json)
        val response = HttpClient.get(apiRequestHelper.baseUrl + "/reports/minimal/run/", headers)
        assertThat(response.statusCode).isEqualTo(404)
    }
}
