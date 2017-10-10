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

            Endpoint("/reports/git/status/", "Git", "status")
                    .json()
                    .secure(setOf("*/reports.run")),

            Endpoint("/reports/git/pull/", "Git", "pull", method = HttpMethod.post)
                    .json()
                    .secure(setOf("*/reports.run")),

            Endpoint("/reports/git/fetch/", "Git", "fetch", method = HttpMethod.post)
                    .json()
                    .secure(setOf("*/reports.run")),

            Endpoint("/reports/", "Report", "getAllNames")
                    .json()
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/", "Report", "getVersionsByName")
                    .json()
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/run/", "Report", "run",
                    method = HttpMethod.post)
                    .json()
                    .secure(setOf("*/reports.run")),

            Endpoint("/reports/:key/status/", "Report", "status")
                    .json()
                    .secure(setOf("*/reports.run")),

            Endpoint("/reports/:name/:version/", "Report", "getByNameAndVersion")
                    .json()
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/:version/all/", "Report", "getZippedByNameAndVersion",
                    ContentTypes.zip)
                    .allowParameterAuthentication()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/:version/publish/", "Report", "publish",
                    method = HttpMethod.post)
                    .json()
                    .secure(setOf("*/reports.review")),

            Endpoint("/reports/:name/:version/artefacts/", "Artefact", "get")
                    .json()
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/:version/artefacts/:artefact/", "Artefact", "download")
                    .secure(setOf("*/reports.read"))
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/resources/", "Resource", "get")
                    .json()
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/reports/:name/:version/resources/:resource/", "Resource", "download")
                    .secure(setOf("*/reports.read"))
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/data/", "Data", "get")
                    .json()
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

            Endpoint("/onetime_token/", "OnetimeToken", "get")
                    .json()
                    .transform()
                    .secure(setOf("*/reports.read")),

            Endpoint("/", "Home", "index")
                    .json()
                    .transform()
    )
}