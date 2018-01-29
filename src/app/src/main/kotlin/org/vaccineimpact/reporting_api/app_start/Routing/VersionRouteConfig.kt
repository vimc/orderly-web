package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.app_start.RouteConfig
import spark.route.HttpMethod

object VersionRouteConfig : RouteConfig
{
    private val readReports = setOf("*/reports.read")
    private val reviewReports = setOf("*/reports.review")

    override val endpoints = listOf(
            Endpoint("/reports/:name/versions/:version/", "Report", "getByNameAndVersion")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/all/", "Report", "getZippedByNameAndVersion",
                    ContentTypes.zip)
                    .allowParameterAuthentication()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/publish/", "Report", "publish",
                    method = HttpMethod.post)
                    .json()
                    .secure(reviewReports),

            Endpoint("/reports/:name/versions/:version/artefacts/", "Artefact", "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/artefacts/:artefact/", "Artefact", "download")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/versions/:version/resources/", "Resource", "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/resources/:resource/", "Resource", "download")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/versions/:version/data/", "Data", "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/data/:data/", "Data", "downloadData")
                    .secure(readReports)
                    .allowParameterAuthentication()
    )
}