package org.vaccineimpact.reporting_api.app_start

import org.vaccineimpact.reporting_api.EndpointDefinition
import org.vaccineimpact.reporting_api.JsonEndpoint
import org.vaccineimpact.reporting_api.Endpoint

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object MontaguRouteConfig: RouteConfig {

    override val endpoints: List<EndpointDefinition> = listOf(

            JsonEndpoint("/reports/", "Report", "getAllNames"),
            JsonEndpoint("/reports/:name/", "Report", "getVersionsByName"),
            JsonEndpoint("/reports/:name/:version/", "Report", "getByNameAndVersion"),
            Endpoint("/reports/:name/:version/all/", "Report", "getZippedByNameAndVersion", "application/zip"),

            JsonEndpoint("/reports/:name/:version/artefacts/", "Artefact", "get"),
            Endpoint("/reports/:name/:version/artefacts/:artefact/", "Artefact", "download", "application/octet-stream"),

            Endpoint("/data/csv/:id/", "Source", "downloadCSV", "text/csv"),
            Endpoint("/data/rds/:id/", "Source", "downloadRDS", "application/octet-stream")
    )
}