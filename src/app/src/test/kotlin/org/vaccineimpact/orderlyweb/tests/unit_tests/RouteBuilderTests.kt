package org.vaccineimpact.orderlyweb.tests.unit_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.app_start.APIRouteConfig
import org.vaccineimpact.orderlyweb.app_start.WebRouteConfig
import org.vaccineimpact.orderlyweb.app_start.routing.api.RunReportRouteConfig
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebRunReportRouteConfig
import org.vaccineimpact.orderlyweb.app_start.routing.web.WebWorkflowRouteConfig

class RouteBuilderTests
{
    @Test
    fun `web routes exclude running routes if noAuth`()
    {
        val routesWithAuth = WebRouteConfig.getEndpoints(true)
        val routesWithoutAuth = WebRouteConfig.getEndpoints(false)

        assertThat(routesWithAuth.toSet() - routesWithoutAuth.toSet()).containsExactlyElementsOf(
                WebRunReportRouteConfig.endpoints +
                        WebWorkflowRouteConfig.endpoints
        )
    }

    @Test
    fun `API routes exclude running routes if noAuth`()
    {
        val routesWithAuth = APIRouteConfig.getEndpoints(true)
        val routesWithoutAuth = APIRouteConfig.getEndpoints(false)

        assertThat(routesWithAuth.toSet() - routesWithoutAuth.toSet()).containsExactlyElementsOf(
                RunReportRouteConfig.endpoints
        )
    }
}