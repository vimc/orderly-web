package org.vaccineimpact.reporting_api.app_start

import org.vaccineimpact.reporting_api.EndpointDefinition
import org.vaccineimpact.reporting_api.app_start.Routing.*

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object MontaguRouteConfig : RouteConfig
{
    override val endpoints: List<EndpointDefinition> =
            ReportRouteConfig.endpoints
                    .plus(VersionRouteConfig.endpoints)
                    .plus(GitRouteConfig.endpoints)
                    .plus(HomeRouteConfig.endpoints)
                    .plus(DataRouteConfig.endpoints)
}