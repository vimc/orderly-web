package org.vaccineimpact.reporting_api.app_start

import org.vaccineimpact.reporting_api.*

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object MontaguRouteConfig : RouteConfig
{
    private val readReports = setOf("*/reports.read")

    override val endpoints: List<EndpointDefinition> = listOf(

            Endpoint("/reports/", "Report", "getAllNames")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/", "Report", "getVersionsByName")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/:version/", "Report", "getByNameAndVersion")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/:version/all/", "Report", "getZippedByNameAndVersion", ContentTypes.zip)
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/artefacts/", "Artefact", "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/:version/artefacts/:artefact/", "Artefact", "download")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/resources/", "Resource", "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/:version/resources/:resource/", "Resource", "download")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/:version/data/", "Data", "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/:version/data/:data/", "Data", "downloadData")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/data/csv/:id/", "Data", "downloadCSV", ContentTypes.csv)
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/data/rds/:id/", "Data", "downloadRDS")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/onetime_token/", "OnetimeToken", "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/", "Home", "index")
                    .json()
                    .transform()
    )
}