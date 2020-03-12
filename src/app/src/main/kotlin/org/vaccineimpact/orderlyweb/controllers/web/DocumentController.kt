package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.repositories.DocumentRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyDocumentRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError
import org.vaccineimpact.orderlyweb.guessFileType
import org.vaccineimpact.orderlyweb.viewmodels.DocumentsViewModel

class DocumentController(context: ActionContext,
                         private val config: Config,
                         private val files: FileSystem,
                         private val docsRepo: DocumentRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, AppConfig(), Files(), OrderlyDocumentRepository())

    @Template("documents.ftl")
    fun getAll(): DocumentsViewModel
    {
        return DocumentsViewModel.build(context, docsRepo.getAllVisibleDocuments())
    }

    fun getDocument(): Boolean
    {
        val path = context.splat()?.joinToString("/")
                ?: throw MissingParameterError("Path to document not provided")

        val fileName = context.splat()!!.last()

        val inline = context.queryParams("inline")?.toBoolean() ?: false

        val absoluteFilePath = "${this.config["documents.root"]}/$path"

        if (!files.fileExists(absoluteFilePath))
        {
            throw OrderlyFileNotFoundError(fileName)
        }

        val response = context.getSparkResponse().raw()

        context.addDefaultResponseHeaders(guessFileType(fileName))
        if (!inline)
        {
            context.addResponseHeader("Content-Disposition", "attachment; filename=\"$path\"")
        }

        files.writeFileToOutputStream(absoluteFilePath, response.outputStream)

        return true
    }
}
