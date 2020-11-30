package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.BundleController
import spark.route.HttpMethod

object BundleRouteConfig : RouteConfig
{
    private val runReports = setOf("*/reports.run")
    private val controller = BundleController::class

    override val endpoints: List<EndpointDefinition> = listOf(
            APIEndpoint("/bundle/pack/:name/", controller, "pack", ContentTypes.zip, HttpMethod.post)
                    .secure(runReports),

            APIEndpoint("/bundle/import/", controller, "import", method = HttpMethod.post)
                    .secure(runReports)
                    .json()
    )
}
