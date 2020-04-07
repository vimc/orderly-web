package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.repositories.DocumentRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyDocumentRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.errors.InvalidOperationError
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError
import org.vaccineimpact.orderlyweb.models.Document
import org.vaccineimpact.orderlyweb.viewmodels.DocumentsViewModel
import java.net.MalformedURLException
import java.net.URL
import java.util.zip.ZipException

class DocumentController(context: ActionContext,
                         private val config: Config,
                         private val files: FileSystem,
                         private val docsRepo: DocumentRepository) : Controller(context)
{
    constructor(context: ActionContext) : this(context, AppConfig(), Files(), OrderlyDocumentRepository())

    private val documentsRoot = files.getAbsolutePath(config["documents.root"])

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

    fun refreshDocuments(): String
    {
        var url = context.postData<String>("url")
        if (url.contains("dropbox"))
        {
            url = url.split("?")[0]
            url = "$url?dl=1"
        }
        val URL = try
        {
            URL(url)
        }
        catch (e: MalformedURLException)
        {
            throw BadRequest("$url is not a valid url")
        }
        try
        {
            files.saveArchiveFromUrl(URL, documentsRoot)
        }
        catch (e: ZipException)
        {
            throw InvalidOperationError("Downloaded file is not a valid zip file")
        }
        val allDocs = docsRepo.getAllFlat()
        val root = DocumentDetails("root", "root", documentsRoot, null, false, false)
        val unrefreshedDocs = allDocs.toMutableList()
        refreshDocumentsInDir(root, unrefreshedDocs)

        //Hide all known docs which were not found
        docsRepo.setVisibility(unrefreshedDocs, false)

        return okayResponse()
    }

    private fun refreshDocumentsInDir(dir: DocumentDetails,
                                      unrefreshedDocs: MutableList<Document>)
    {
        val children = files.getAllChildren(dir.absolutePath, documentsRoot)
        for (child in children)
        {
            val docForChild = unrefreshedDocs.find { it.path == child.pathFragment }

            if (docForChild != null)
            {
                docsRepo.setVisibility(listOf(docForChild), true)
                unrefreshedDocs.remove(docForChild)
            }
            else
            {
                docsRepo.add(child.pathFragment!!, child.name, child.displayName, child.isFile, child.external, dir.pathFragment)
            }

            if (!child.isFile)
            {
                refreshDocumentsInDir(child, unrefreshedDocs)
            }
        }
    }
}
