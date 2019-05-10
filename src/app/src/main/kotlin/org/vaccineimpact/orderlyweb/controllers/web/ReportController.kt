package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel

class ReportController: OrderlyDataController
{
    constructor(actionContext: ActionContext,
                orderly: OrderlyClient): super(actionContext, orderly)

    constructor(actionContext: ActionContext): super(actionContext)

    open class ReportViewModel(@Serialise("reportJson") open val report: ReportVersionDetails,
                               open val focalArtefactUrl: String?,
                               open val isAdmin: Boolean,
                               context: ActionContext) : AppViewModel(context)

    @Template("report-page.ftl")
    fun getByNameAndVersion(): ReportViewModel
    {
        val reportName = context.params(":name")
        val version = context.params(":version")
        val reportDetails = orderly.getDetailsByNameAndVersion(reportName, version)
        val displayName = reportDetails.displayName ?: reportDetails.name

        val focalArtefact = reportDetails.artefacts.firstOrNull {
            it.files.any { f -> canRenderInBrowser(f) }
        }?.files?.firstOrNull { canRenderInBrowser(it) }

        val focalArtefactUrl = if (focalArtefact == null)
        {
            null
        }
        else
        {
            "/reports/$reportName/versions/$version/artefacts/$focalArtefact?inline=true"
        }

        val isAdmin = context.hasPermission(ReifiedPermission("reports.review", Scope.Global()))
        return ReportViewModel(reportDetails.copy(displayName = displayName), focalArtefactUrl, isAdmin, context)
    }

    fun canRenderInBrowser(fileName: String): Boolean
    {
        val ext = fileName.toLowerCase().split(".").last()
        return arrayOf("png", "jpg", "jpeg", "gif", "svg", "pdf", "html", "htm")
                .contains(ext)
    }
}