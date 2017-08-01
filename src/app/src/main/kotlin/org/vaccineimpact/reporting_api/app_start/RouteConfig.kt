package org.vaccineimpact.reporting_api.app_start

import org.vaccineimpact.reporting_api.*

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object MontaguRouteConfig : RouteConfig
{

    override val endpoints: List<EndpointDefinition> = listOf(

            JsonEndpoint("/reports/", "Report", "getAllNames")
                    .secure(),

            JsonEndpoint("/reports/:name/", "Report", "getVersionsByName")
                    .secure(),

            JsonEndpoint("/reports/:name/:version/", "Report", "getByNameAndVersion")
                    .secure(),

            Endpoint("/reports/:name/:version/all/", "Report", "getZippedByNameAndVersion", ContentTypes.zip)
                    .secure()
                    .allowParameterAuthentication(),

            JsonEndpoint("/reports/:name/:version/artefacts/", "Artefact", "get")
                    .secure(),

            Endpoint("/reports/:name/:version/artefacts/:artefact/", "Artefact", "download")
                    .secure()
                    .allowParameterAuthentication(),

            JsonEndpoint("/reports/:name/:version/resources/", "Resource", "get")
                    .secure(),

            Endpoint("/reports/:name/:version/resources/:resource/", "Resource", "download")
                    .secure()
                    .allowParameterAuthentication(),

            JsonEndpoint("/reports/:name/:version/data/", "Data", "get")
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

            JsonEndpoint("/onetime_token/", "OnetimeToken", "get")
                    .secure(),

            JsonEndpoint("/", "Home", "index")
    )
}