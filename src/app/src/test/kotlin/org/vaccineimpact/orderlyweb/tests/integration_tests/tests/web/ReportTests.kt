package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class ReportTests : IntegrationTest()
{
    @Test
    fun `only report runners can run report`()
    {
        val url = "/report/minimal/run"
        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.post)
    }

    @Test
    fun `only report runners can get running report status`()
    {

        val url = "/report/frightened_rabbit/status"
        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.post)
    }
}