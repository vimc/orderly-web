package org.vaccineimpact.reporting_api.app_start.Routing

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.app_start.RouteConfig
import org.vaccineimpact.reporting_api.controllers.ArtefactController
import org.vaccineimpact.reporting_api.controllers.DataController
import org.vaccineimpact.reporting_api.controllers.ReportController
import org.vaccineimpact.reporting_api.controllers.ResourceController
import org.vaccineimpact.reporting_api.controllers.VersionController
import spark.route.HttpMethod

object VersionRouteConfig : RouteConfig
{
    private val readReports = setOf("report:<name>/reports.read")
    private val reviewReports = setOf("*/reports.review")
    private val artefactController = ArtefactController::class
    private val reportController = ReportController::class
    private val dataController = DataController::class
    private val resourceController = ResourceController::class
    private val versionController = VersionController::class

    override val endpoints = listOf(
            Endpoint("/versions/", reportController, "getAllVersions")
                    .json()
                    .transform()
                    // more specific permission checking in the controller action
                    .secure(),

            Endpoint("/reports/:name/versions/:version/", versionController, "getByNameAndVersion")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/all/", versionController, "getZippedByNameAndVersion",
                    ContentTypes.zip)
                    .allowParameterAuthentication()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/publish/", reportController, "publish",
                    method = HttpMethod.post)
                    .json()
                    .secure(reviewReports),
            Endpoint("/reports/:name/versions/:version/changelog/", versionController, "getChangelogByNameAndVersion")
                    .json()
                    .secure(reviewReports)
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/versions/:version/artefacts/", artefactController, "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/artefacts/:artefact/", artefactController, "download")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/versions/:version/resources/", resourceController, "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/resources/:resource/", resourceController, "download")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            Endpoint("/reports/:name/versions/:version/data/", dataController, "get")
                    .json()
                    .transform()
                    .secure(readReports),

            Endpoint("/reports/:name/versions/:version/data/:data/", dataController, "downloadData")
                    .secure(readReports)
                    .allowParameterAuthentication()
    )
}