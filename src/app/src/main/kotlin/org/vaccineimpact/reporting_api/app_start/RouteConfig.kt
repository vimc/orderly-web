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

            Endpoint("/reports/git/status/", "Git", "status", ContentTypes.json)
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/git/pull/", "Git", "pull", ContentTypes.json, method = HttpMethod.post)
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/git/fetch/", "Git", "fetch", ContentTypes.json, method = HttpMethod.post)
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/", "Report", "getAllNames", ContentTypes.json)
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/run/", "Report", "run", ContentTypes.json,
                    method = HttpMethod.post)
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:key/status/", "Report", "status", ContentTypes.json)
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/", "Report", "getVersionsByName",ContentTypes.json)
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/:version/", "Report", "getByNameAndVersion", ContentTypes.json)
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/:version/publish/", "Report", "publish", ContentTypes.json,
                    method = HttpMethod.post)
                    .secure(setOf("*/reports.read", "*/reports.review"  )),

            Endpoint("/reports/:name/:version/all/", "Report", "getZippedByNameAndVersion",
                    ContentTypes.zip)
                    .secure(setOf("*/reports.read"))
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/artefacts/", "Artefact", "get", ContentTypes.json)
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/:version/artefacts/:artefact/", "Artefact", "download")
                    .secure(setOf("*/reports.read"))
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/resources/", "Resource", "get", ContentTypes.json)
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/:version/resources/:resource/", "Resource", "download")
                    .secure(setOf("*/reports.read"))
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/data/", "Data", "get", ContentTypes.json)
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/:version/data/:data/", "Data", "downloadData")
                    .secure(setOf("*/reports.read"))
                    .allowParameterAuthentication(),

            Endpoint("/data/csv/:id/", "Data", "downloadCSV", ContentTypes.csv)
                    .secure(setOf("*/reports.read"))
                    .allowParameterAuthentication(),

            Endpoint("/data/rds/:id/", "Data", "downloadRDS")
                    .secure(setOf("*/reports.read"))
                    .allowParameterAuthentication(),

            Endpoint("/onetime_token/", "OnetimeToken", "get", ContentTypes.json)
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/", "Home", "index", ContentTypes.json)
                    .transform()
    )
}