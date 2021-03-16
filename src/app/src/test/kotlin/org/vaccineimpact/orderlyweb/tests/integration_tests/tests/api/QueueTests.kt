package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import com.fasterxml.jackson.databind.node.ArrayNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
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

        JSONValidator.validateAgainstSchema(response.text, "QueueStatus")
        val tasks = JSONValidator.getData(response.text)["tasks"] as ArrayNode
        assertThat(tasks.count()).isEqualTo(1)
        assertThat(tasks[0]["name"].textValue()).isEqualTo("minimal")
        assertThat(tasks[0]["key"].textValue()).isNotEmpty()
        assertThat(tasks[0]["status"].textValue()).isNotEmpty()
    }
}
