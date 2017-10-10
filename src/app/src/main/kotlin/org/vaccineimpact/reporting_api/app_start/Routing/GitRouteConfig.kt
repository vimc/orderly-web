package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.Endpoint
import org.vaccineimpact.reporting_api.EndpointDefinition
import org.vaccineimpact.reporting_api.app_start.RouteConfig
import org.vaccineimpact.reporting_api.json
import org.vaccineimpact.reporting_api.secure
import spark.route.HttpMethod

object GitRouteConfig : RouteConfig
{

    override val endpoints: List<EndpointDefinition> = listOf(
            Endpoint("/reports/git/status/", "Git", "status")
                    .json()
                    .secure(setOf("*/reports.run")),

            Endpoint("/reports/git/pull/", "Git", "pull", method = HttpMethod.post)
                    .json()
                    .secure(setOf("*/reports.run")),

            Endpoint("/reports/git/fetch/", "Git", "fetch", method = HttpMethod.post)
                    .json()
                    .secure(setOf("*/reports.run"))
    )

}