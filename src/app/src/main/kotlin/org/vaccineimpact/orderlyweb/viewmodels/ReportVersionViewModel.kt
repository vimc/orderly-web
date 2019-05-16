package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails

open class ReportVersionViewModel(@Serialise("reportJson") open val report: ReportVersionDetails,
                                  open val focalArtefactUrl: String?,
                                  open val isAdmin: Boolean,
                                  open val artefacts: List<ArtefactViewModel>,
                                  open val dataLinks: List<InputDataViewModel>,
                                  open val resources: List<DownloadableFileViewModel>,
                                  open val zipFile: DownloadableFileViewModel,
                                  context: ActionContext) : AppViewModel(context)


class ArtefactViewModel(val artefact: Artefact, val files: List<DownloadableFileViewModel>, val inlineArtefactFigure: String?)

class InputDataViewModel(val key: String, val csv: DownloadableFileViewModel, val rds: DownloadableFileViewModel)

class DownloadableFileViewModel(val name: String, val url: String)
