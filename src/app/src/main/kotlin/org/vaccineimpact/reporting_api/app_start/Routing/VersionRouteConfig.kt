package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.Endpoint
import org.vaccineimpact.reporting_api.app_start.RouteConfig
import org.vaccineimpact.reporting_api.json
import org.vaccineimpact.reporting_api.secure
import org.vaccineimpact.reporting_api.transform
import org.vaccineimpact.reporting_api.allowParameterAuthentication
import spark.route.HttpMethod

object VersionRouteConfig : RouteConfig
{
    private val readReports = setOf("*/reports.read")
    private val reviewReports = setOf("*/reports.review")

    override val endpoints = listOf(
            Endpoint("/reports/:name/:version/", "Report", "getByNameAndVersion")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/:version/all/", "Report", "getZippedByNameAndVersion",
                    ContentTypes.zip)
                    .allowParameterAuthentication()
                    .secure(readReports),

            Endpoint("/reports/:name/:version/publish/", "Report", "publish",
                    method = HttpMethod.post)
                    .json()
                    .secure(reviewReports),

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
                    .allowParameterAuthentication()
    )
}