package org.vaccineimpact.reporting_api.app_start

import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.EndpointDefinition
import org.vaccineimpact.reporting_api.JsonEndpoint
import org.vaccineimpact.reporting_api.OnetimeTokenEndpoint

interface RouteConfig {
    val endpoints: List<EndpointDefinition>
}

object MontaguRouteConfig : RouteConfig {

    override val endpoints: List<EndpointDefinition> = listOf(

            JsonEndpoint("/reports/", "Report", "getAllNames"),
            JsonEndpoint("/reports/:name/", "Report", "getVersionsByName"),
            JsonEndpoint("/reports/:name/:version/", "Report", "getByNameAndVersion"),
            OnetimeTokenEndpoint("/reports/:name/:version/all/", "Report", "getZippedByNameAndVersion", ContentTypes.zip),

            JsonEndpoint("/reports/:name/:version/artefacts/", "Artefact", "get"),
            OnetimeTokenEndpoint("/reports/:name/:version/artefacts/:artefact/", "Artefact", "download"),

            JsonEndpoint("/reports/:name/:version/resources/", "Resource", "get"),
            OnetimeTokenEndpoint("/reports/:name/:version/resources/:resource/", "Resource", "download"),

            JsonEndpoint("/reports/:name/:version/data/", "Data", "get"),
            OnetimeTokenEndpoint("/reports/:name/:version/data/:data/", "Data", "downloadData"),

            OnetimeTokenEndpoint("/data/csv/:id/", "Data", "downloadCSV", ContentTypes.csv),
            OnetimeTokenEndpoint("/data/rds/:id/", "Data", "downloadRDS"),

            JsonEndpoint("/access_token/", "OnetimeToken", "get")
    )
}