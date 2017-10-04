package org.vaccineimpact.reporting_api.app_start

import org.vaccineimpact.reporting_api.*
import spark.route.HttpMethod

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object MontaguRouteConfig : RouteConfig
{

    override val endpoints: List<EndpointDefinition> = listOf(

            Endpoint("/reports/", "Report", "getAllNames", ContentTypes.json)
                    .transform()
                    .secure(),

            Endpoint("/reports/:name/run/", "Report", "run", ContentTypes.json,
                    method = HttpMethod.post)
                    .secure(),

            Endpoint("/reports/:key/status/", "Report", "status", ContentTypes.json)
                    .secure(),

            Endpoint("/reports/:name/", "Report", "getVersionsByName",ContentTypes.json)
                    .transform()
                    .secure(),

            Endpoint("/reports/:name/:version/", "Report", "getByNameAndVersion", ContentTypes.json)
                    .transform()
                    .secure(),

            Endpoint("/reports/:name/:version/publish/", "Report", "publish", ContentTypes.json,
                    method = HttpMethod.post)
                    .secure(),

            Endpoint("/reports/:name/:version/all/", "Report", "getZippedByNameAndVersion",
                    ContentTypes.zip)
                    .secure()
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/artefacts/", "Artefact", "get", ContentTypes.json)
                    .transform()
                    .secure(),

            Endpoint("/reports/:name/:version/artefacts/:artefact/", "Artefact", "download")
                    .secure()
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/resources/", "Resource", "get", ContentTypes.json)
                    .transform()
                    .secure(),

            Endpoint("/reports/:name/:version/resources/:resource/", "Resource", "download")
                    .secure()
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/data/", "Data", "get", ContentTypes.json)
                    .transform()
                    .secure(),

            Endpoint("/reports/:name/:version/data/:data/", "Data", "downloadData")
                    .secure()
                    .allowParameterAuthentication(),

            Endpoint("/data/csv/:id/", "Data", "downloadCSV", ContentTypes.csv)
                    .secure()
                    .allowParameterAuthentication(),

            Endpoint("/data/rds/:id/", "Data", "downloadRDS")
                    .secure()
                    .allowParameterAuthentication(),

            Endpoint("/onetime_token/", "OnetimeToken", "get", ContentTypes.json)
                    .transform()
                    .secure(),

            Endpoint("/", "Home", "index", ContentTypes.json)
                    .transform()
    )
}