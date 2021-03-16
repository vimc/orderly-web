package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

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
        val runResponse = apiRequestHelper.post("/reports/minimal/run/",
                mapOf("params" to mapOf<String, String>()),
                userEmail = fakeGlobalReportReviewer())

        val response = apiRequestHelper.get("/queue/status", userEmail = fakeGlobalReportReader())
        assertThat(response.text).isEqualTo("argh")
        //Actual   :"{"data":{"tasks":[{"name":"minimal","version":null,"key":"lightsome_blackmamba","status":"running"}]},"errors":[],"status":"success"}"
    }
}
