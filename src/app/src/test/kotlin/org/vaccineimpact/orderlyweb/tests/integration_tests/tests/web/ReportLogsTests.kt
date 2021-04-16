package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.giveUserGroupPermission
import org.vaccineimpact.orderlyweb.tests.insertUser
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import com.fasterxml.jackson.databind.node.ArrayNode
import com.sun.mail.handlers.text_plain
import org.vaccineimpact.orderlyweb.models.ReportRunWithDate
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebReportRunRepository

class ReportLogsTests : IntegrationTest()
{
    @Test
    fun `can get all running reports`()
    {
        val url = "/reports/running/"
        val permissions = setOf(ReifiedPermission("reports.run", Scope.Global()))

        val sessionCookie = webRequestHelper.webLoginWithMontagu(permissions)
        val contentType = ContentTypes.json

        val now = Instant.now()
        val now2 = Instant.now()

        OrderlyWebReportRunRepository().addReportRun(
            "key1",
            "test.user@example.com",
            now,
            "report1",
            mapOf("instance1" to "pre-staging"),
            mapOf("parameter1" to "value1"),
            "branch1",
            "commit1"
        )

        OrderlyWebReportRunRepository().addReportRun(
            "key2",
            "test.user@example.com",
            now2,
            "report2",
            mapOf("instance1" to "pre-staging"),
            mapOf("parameter1" to "value1"),
            "branch1",
            "commit1"
        )
         
        val response = webRequestHelper.requestWithSessionCookie(url, sessionCookie, contentType)

        assertSuccessful(response)
        assertJsonContentType(response)

        val result = JSONValidator.getData(response.text) as ArrayNode
        assertThat(result.count()).isEqualTo(2)
        assertThat(result[0]["date"].textValue()).isEqualTo(now.toString())
        assertThat(result[0]["name"].textValue()).isEqualTo("report1")
        assertThat(result[0]["key"].textValue()).isEqualTo("key1")
        assertThat(result[1]["date"].textValue()).isEqualTo(now2.toString())
        assertThat(result[1]["name"].textValue()).isEqualTo("report2")
        assertThat(result[1]["key"].textValue()).isEqualTo("key2")
    }

    @Test
    fun `only users with permissions can get running reports`()
    {
        val url = "/reports/running/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())),
                contentType = ContentTypes.json)
    }
}