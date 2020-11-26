package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.APIEndpoint
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.EndpointDefinition
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.BundleController
import org.vaccineimpact.orderlyweb.json
import org.vaccineimpact.orderlyweb.secure
import spark.route.HttpMethod

object BundleRouteConfig : RouteConfig {
    private val runReports = setOf("*/reports.run")
    private val controller = BundleController::class

    override val endpoints: List<EndpointDefinition> = listOf(
        APIEndpoint("/bundle/pack/:name/", controller, "pack", ContentTypes.zip, HttpMethod.post)
            .secure(runReports),

        APIEndpoint("/bundle/import/", controller, "import", method = HttpMethod.post)
            .json()
            .secure(runReports)
    )
}
