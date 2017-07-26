package org.vaccineimpact.reporting_api.app_start

import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.Endpoint
import org.vaccineimpact.reporting_api.EndpointDefinition
import org.vaccineimpact.reporting_api.JsonEndpoint

interface RouteConfig {
    val endpoints: List<EndpointDefinition>
}

object MontaguRouteConfig : RouteConfig {

    override val endpoints: List<EndpointDefinition> = listOf(

            JsonEndpoint("/reports/", "Report", "getAllNames"),
            JsonEndpoint("/reports/:name/", "Report", "getVersionsByName"),
            JsonEndpoint("/reports/:name/:version/", "Report", "getByNameAndVersion"),
            Endpoint("/reports/:name/:version/all/", "Report", "getZippedByNameAndVersion", ContentTypes.zip),

            JsonEndpoint("/reports/:name/:version/artefacts/", "Artefact", "get"),
            Endpoint("/reports/:name/:version/artefacts/:artefact/", "Artefact", "download"),

            JsonEndpoint("/reports/:name/:version/resources/", "Resource", "get"),
            Endpoint("/reports/:name/:version/resources/:resource/", "Resource", "download"),

            JsonEndpoint("/reports/:name/:version/data/", "Data", "get"),
            Endpoint("/reports/:name/:version/data/:data/", "Data", "downloadData"),

            Endpoint("/data/csv/:id/", "Data", "downloadCSV", ContentTypes.csv),
            Endpoint("/data/rds/:id/", "Data", "downloadRDS")
    )
}