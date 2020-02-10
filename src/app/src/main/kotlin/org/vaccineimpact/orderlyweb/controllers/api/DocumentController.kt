package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.DocumentDetails
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.DocumentRepository
import org.vaccineimpact.orderlyweb.db.OrderlyDocumentRepository
import org.vaccineimpact.orderlyweb.models.Document
import java.io.File

class DocumentController(context: ActionContext,
                         private val fileSystem: FileSystem,
                         config: Config,
                         private val repo: DocumentRepository) : Controller(context)
{
    constructor(context: ActionContext) :
            this(context,
                    Files(),
                    AppConfig(),
                    OrderlyDocumentRepository())

    private val documentsRoot = fileSystem.getAbsolutePath(config["documents.root"])

    fun refreshDocuments(): String
    {
        val allDocs = repo.getAllFlat()
        val root = DocumentDetails("root", documentsRoot, null)
        val unrefreshedDocs = allDocs.toMutableList()
        refreshDocumentsInDir(root, unrefreshedDocs)

        //Hide all known docs which were not found
        unrefreshedDocs.forEach { repo.setVisibility(it, false) }

        return okayResponse()
    }

    private fun refreshDocumentsInDir(document: DocumentDetails,
                                      unrefreshedDocs: MutableList<Document>)
    {
        val files = fileSystem.getAllChildren(document.absolutePath, documentsRoot)
        refreshDirChildren(document, files, unrefreshedDocs)
    }

    private fun refreshDirChildren(parent: DocumentDetails,
                                   children: List<DocumentDetails>,
                                   unrefreshedDocs: MutableList<Document>)
    {
        for (child in children)
        {
            val docForChild = unrefreshedDocs.find { it.path == child.pathFragment }

            if (docForChild != null)
            {
                repo.setVisibility(docForChild, true)
                unrefreshedDocs.remove(docForChild)
            }
            else
            {
                repo.add(child.pathFragment!!, child.name, File(child.absolutePath).isFile, parent.pathFragment)
            }

            refreshDocumentsInDir(child, unrefreshedDocs)
        }
    }

}