package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.app_start.routing.api.*
import org.vaccineimpact.orderlyweb.app_start.routing.web.*
import org.vaccineimpact.orderlyweb.controllers.web.AdminController
import org.vaccineimpact.orderlyweb.controllers.web.IndexController

interface RouteBuilder
{
    fun getEndpoints(useAuth: Boolean): List<EndpointDefinition>
}

interface RouteConfig
{
    val endpoints: List<EndpointDefinition>
}

object APIRouteConfig : RouteBuilder
{
    override fun getEndpoints(useAuth: Boolean): List<EndpointDefinition>
    {
        val endpoints = GitRouteConfig.endpoints.plus(ReportRouteConfig.endpoints)
                .plus(VersionRouteConfig.endpoints)
                .plus(HomeRouteConfig.endpoints)
                .plus(DataRouteConfig.endpoints)
                .plus(UserRouteConfig.endpoints)
                .plus(BundleRouteConfig.endpoints)
                .plus(QueueRouteConfig.endpoints)
                .plus(OutpackRouteConfig.endpoints).toMutableList()

        if (useAuth)
        {
            endpoints += RunReportRouteConfig.endpoints
        }
        return endpoints
    }
}

object WebRouteConfig : RouteBuilder
{
    private val legacyEndpoint = WebEndpoint(
            "/api/v1/*/",
            IndexController::class,
            "legacy"
    ).json().transform()

    private val metricsEndpoint = WebEndpoint(
            "/metrics/",
            IndexController::class,
            "metrics",
            contentType = ContentTypes.text
    )

    private val accessibilityEndpoint = WebEndpoint(
            "/accessibility/",
            IndexController::class,
            "accessibility"
    )

    private val adminEndpoint = WebEndpoint(
            "/manage-access/",
            AdminController::class,
            "admin"
    )
            .secure(setOf("*/users.manage"))

    override fun getEndpoints(useAuth: Boolean): List<EndpointDefinition>
    {
        val endpoints = (
                WebAuthRouteConfig.endpoints +
                WebDocumentRouteConfig.endpoints +
                WebReportRouteConfig.endpoints +
                WebLogsRouteConfig.endpoints +
                WebVersionRouteConfig.endpoints +
                WebUserRouteConfig.endpoints +
                WebPermissionRouteConfig.endpoints +
                WebRoleRouteConfig.endpoints +
                WebSettingsRouteConfig.endpoints +
                WebGitRouteConfig.endpoints + metricsEndpoint + adminEndpoint +
                accessibilityEndpoint + legacyEndpoint
                ).toMutableList()

        if (useAuth)
        {
            endpoints += WebRunReportRouteConfig.endpoints +
                    WebWorkflowRouteConfig.endpoints
        }

        return endpoints
    }
}
