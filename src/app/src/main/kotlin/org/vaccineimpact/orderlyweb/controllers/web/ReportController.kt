package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel

class ReportController(actionContext: ActionContext,
                       private val orderly: OrderlyClient) : Controller(actionContext)
{
    constructor(actionContext: ActionContext) : this(actionContext, Orderly())

    class ReportViewModel(@Serialise("reportJson") val report: ReportVersionDetails,
                          val focalArtefactUrl: String?,
                          context: ActionContext) : AppViewModel(context)

    @Template("report.ftl")
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
        return ReportViewModel(reportDetails.copy(displayName = displayName), focalArtefactUrl, context)
    }

    fun canRenderInBrowser(fileName: String): Boolean
    {
        val ext = fileName.toLowerCase().split(".").last()
        return arrayOf("png", "jpg", "jpeg", "gif", "svg", "pdf", "html", "htm")
                .contains(ext)
    }
}