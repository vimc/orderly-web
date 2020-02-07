package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
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
                         private val repo: DocumentRepository): Controller(context)
{
    constructor(context: ActionContext) :
            this(context,
                    Files(),
                    AppConfig(),
                    OrderlyDocumentRepository())

    private val topLevelFolder = fileSystem.getAbsolutePath(config["documents.root"])

    fun refreshDocuments(): String {
        val allDocs = repo.getAllFlat()

        val unrefreshedDocs = allDocs.toMutableList()

        val topLevelDir = unrefreshedDocs.find{it.path == relativePath(topLevelFolder)}
        if ( topLevelDir == null)
        {
            repo.add(relativePath(topLevelFolder), documentName(topLevelFolder), false, null)
        }
        else
        {
            unrefreshedDocs.remove(topLevelDir)
        }
        refreshDocumentsInDir(topLevelFolder,  unrefreshedDocs)

        //Hide all known docs which were not found
        unrefreshedDocs.filter{ it.show }.forEach{ repo.setVisibility(it, false) }

        return okayResponse()
    }

    private fun refreshDocumentsInDir(absolutePath: String,
                                      unrefreshedDocs: MutableList<Document>)
    {
        val relativeParent = formatFolder(relativePath(absolutePath))

        val files = fileSystem.getChildFiles(absolutePath)
        val folders = fileSystem.getChildFolders(absolutePath)

        val refreshChildren = {children: List<String>, areFolders: Boolean ->
            for (childPath in children)
            {
                var child = relativePath(childPath)
                if (areFolders)
                {
                    child = formatFolder(child);
                }

                val docForChild = unrefreshedDocs.find { it.path == child }

                if (docForChild != null)
                {
                    if (!docForChild.show)
                    {
                        repo.setVisibility(docForChild, true)
                    }
                    unrefreshedDocs.remove(docForChild)
                }
                else
                {
                    repo.add(child, documentName(child), !areFolders, relativeParent)
                }

                if (areFolders)
                {
                    refreshDocumentsInDir(childPath, unrefreshedDocs)
                }
            }
        }

        refreshChildren(files, false)
        refreshChildren(folders, true)
    }

    private fun relativePath(absolutePath: String): String
    {
        var result =  absolutePath.removePrefix(topLevelFolder)
        if (!result.startsWith("/"))
        {
            result = "/$result";
        }
        return result
    }

    private fun formatFolder(folder: String): String
    {
        var result = folder
        if (!result.startsWith("/"))
        {
            result = "/$result";
        }
        if (!result.endsWith("/"))
        {
            result = "$result/"
        }

        return result;
    }

    private fun documentName(path: String): String
    {
        val result = path.removeSuffix("/").split("/").last()
        return result.removeSuffix("/")
    }

}