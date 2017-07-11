package org.vaccineimpact.reporting_api.app_start

import org.vaccineimpact.reporting_api.EndpointDefinition
import org.vaccineimpact.reporting_api.JsonEndpoint

object RouteConfig {

    val endpoints: List<EndpointDefinition> = listOf(

            JsonEndpoint("/reports/", "Report", "getAll"),
            JsonEndpoint("/reports/:name/", "Report", "getByName"),
            JsonEndpoint("/reports/:name/:version/", "Report", "getByNameAndVersion"),

            JsonEndpoint("/reports/:name/:version/artefacts/", "Artefact", "getAll")
    )
}
