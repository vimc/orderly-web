package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class IndexPageTests : IntegrationTest()
{
    @Test
    fun `report readers can get index page`()
    {
        assertWebUrlSecured("/", setOf(ReifiedPermission("reports.read", Scope.Global())))
    }
}