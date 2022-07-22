package org.vaccineimpact.orderlyweb.app_start.routing.api

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.RouteConfig
import org.vaccineimpact.orderlyweb.controllers.api.ArtefactController
import org.vaccineimpact.orderlyweb.controllers.api.DataController
import org.vaccineimpact.orderlyweb.controllers.api.ReportController
import org.vaccineimpact.orderlyweb.controllers.api.ResourceController
import org.vaccineimpact.orderlyweb.controllers.api.VersionController
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
            APIEndpoint("/versions/", reportController, "getAllVersions")
                    .json()
                    .transform()
                    // more specific permission checking in the controller action
                    .secure(),

            APIEndpoint("/reports/:name/versions/:version/", versionController, "getByNameAndVersion")
                    .json()
                    .transform()
                    .secure(readReports),

            APIEndpoint("/reports/:name/versions/:version/all/", versionController, "getZippedByNameAndVersion",
                    ContentTypes.zip)
                    .allowParameterAuthentication()
                    .secure(readReports),

            APIEndpoint("/reports/:name/versions/:version/publish/", reportController, "publish",
                    method = HttpMethod.post)
                    .json()
                    .transform()
                    .secure(reviewReports),

            APIEndpoint("/reports/:name/versions/:version/changelog/", versionController,
                    "getChangelogByNameAndVersion")
                    .json()
                    .transform()
                    .secure(readReports)
                    .allowParameterAuthentication(),

            APIEndpoint("/reports/:name/versions/:version/artefacts/", artefactController, "getMetaData")
                    .json()
                    .transform()
                    .secure(readReports),

            APIEndpoint("/reports/:name/versions/:version/artefacts/:artefact/", artefactController, "getFile")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            APIEndpoint("/reports/:name/versions/:version/resources/", resourceController, "get")
                    .json()
                    .transform()
                    .secure(readReports),

            APIEndpoint("/reports/:name/versions/:version/resources/:resource/", resourceController, "download")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            APIEndpoint("/reports/:name/versions/:version/data/", dataController, "get")
                    .json()
                    .transform()
                    .secure(readReports),

            APIEndpoint("/reports/:name/versions/:version/data/:data/", dataController, "downloadData")
                    .secure(readReports)
                    .allowParameterAuthentication(),

            APIEndpoint("/reports/:name/versions/:version/data/:data/", dataController, "downloadData",
                    contentType = ContentTypes.csv)
                    .secure(readReports)
                    .allowParameterAuthentication(),

            APIEndpoint("/reports/:name/versions/:version/run-meta/", versionController, "getRunMetadata",
                    contentType = ContentTypes.binarydata)
                    .secure(readReports)
                    .allowParameterAuthentication()
    )
}
