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
        val root = DocumentDetails("root", documentsRoot, null, false, false)
        val unrefreshedDocs = allDocs.toMutableList()
        refreshDocumentsInDir(root, unrefreshedDocs)

        //Hide all known docs which were not found
        repo.setVisibility(unrefreshedDocs, false)

        return okayResponse()
    }

    private fun refreshDocumentsInDir(dir: DocumentDetails,
                                      unrefreshedDocs: MutableList<Document>)
    {
        val children = fileSystem.getAllChildren(dir.absolutePath, documentsRoot)
        for (child in children)
        {
            val docForChild = unrefreshedDocs.find { it.path == child.pathFragment }

            if (docForChild != null)
            {
                repo.setVisibility(listOf(docForChild), true)
                unrefreshedDocs.remove(docForChild)
            }
            else
            {
                repo.add(child.pathFragment!!, child.name, child.isFile, child.external, dir.pathFragment)
            }

            if (!child.isFile)
            {
                refreshDocumentsInDir(child, unrefreshedDocs)
            }
        }
    }

}