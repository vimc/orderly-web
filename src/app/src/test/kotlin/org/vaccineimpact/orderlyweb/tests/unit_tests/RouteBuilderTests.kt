package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.app_start.APIRouteBuilder
import org.vaccineimpact.orderlyweb.app_start.WebRouteBuilder
import org.vaccineimpact.orderlyweb.app_start.routing.api.RunReportRouteConfig
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebRunReportRouteConfig
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebWorkflowRouteConfig

class RouteBuilderTests
{
    @Test
    fun `web routes exclude running routes if noAuth`()
    {
        val routesWithAuth = WebRouteBuilder.getEndpoints(true)
        val routesWithoutAuth = WebRouteBuilder.getEndpoints(false)

        assertThat(routesWithAuth.toSet() - routesWithoutAuth.toSet()).containsExactlyElementsOf(
                WebRunReportRouteConfig.endpoints +
                        WebWorkflowRouteConfig.endpoints
        )
    }

    @Test
    fun `API routes exclude running routes if noAuth`()
    {
        val routesWithAuth = APIRouteBuilder.getEndpoints(true)
        val routesWithoutAuth = APIRouteBuilder.getEndpoints(false)

        assertThat(routesWithAuth.toSet() - routesWithoutAuth.toSet()).containsExactlyElementsOf(
                RunReportRouteConfig.endpoints
        )
    }
}
