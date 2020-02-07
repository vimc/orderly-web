package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.DocumentRepository
import org.vaccineimpact.orderlyweb.models.Document

class DocumentController(context: ActionContext,
                         private val fileSystem: FileSystem = Files(),
                         private val config: Config = AppConfig(),
                         private val repo: DocumentRepository): Controller(context)
{
    private val topLevelFolder = formatFolder(config["documents.location"])

    fun refreshDocuments() {
        val allDocs = repo.getAllFlat()

        val unrefreshedDocs = allDocs.filter{it.isFile}.toMutableList()
        val unrefreshedDirs = allDocs.filter{!it.isFile}.toMutableList()

        val topLevelDir = unrefreshedDirs.find{it.path == relativePath(topLevelFolder)}
        if ( topLevelDir == null)
        {
            repo.add(relativePath(topLevelFolder), documentName(topLevelFolder), false, null)
        }
        else
        {
            unrefreshedDirs.remove(topLevelDir)
        }
        refreshDocumentsInDir(topLevelFolder,  unrefreshedDocs, unrefreshedDirs)

        //Hide all known files and folders which were not found
        unrefreshedDocs.filter{ it.show }.forEach{ repo.setVisibility(it, false) }
        unrefreshedDirs.filter{ it.show }.forEach{ repo.setVisibility(it, false) }
    }

    private fun refreshDocumentsInDir(absolutePath: String, //this needs to be absolute
                                      unrefreshedDocs: MutableList<Document>,
                                      unrefreshedDirs: MutableList<Document>)
    {
        val relativeParent = formatFolder(relativePath(absolutePath))

        val files = fileSystem.getChildFiles(absolutePath).map{relativePath(it)}
        for(file in files)
        {
            val docForFile = unrefreshedDocs.find{it.path == file}

            if (docForFile != null)
            {
                if (!docForFile.show)
                {
                    repo.setVisibility(docForFile, true)
                }
               unrefreshedDocs.remove(docForFile)
            }
            else
            {
                val name = documentName(file)
                repo.add(file, name, true, relativeParent)
            }
        }

        val folders = fileSystem.getChildFolders(absolutePath)
        for(folderPath in folders)
        {
            val folder = formatFolder(relativePath(folderPath))
            val docForFolder = unrefreshedDirs.find{it.path == folder}
            if (docForFolder != null)
            {
                if (!docForFolder.show)
                {
                    repo.setVisibility(docForFolder, true)
                }
                unrefreshedDirs.remove(docForFolder)
            }
            else
            {
                val name = documentName(folder)
                repo.add(folder, name, false, relativeParent)
            }

            refreshDocumentsInDir(folderPath, unrefreshedDocs, unrefreshedDirs)
        }
    }

    private fun relativePath(absolutePath: String): String
    {
        return "/${absolutePath.removePrefix(topLevelFolder)}"
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