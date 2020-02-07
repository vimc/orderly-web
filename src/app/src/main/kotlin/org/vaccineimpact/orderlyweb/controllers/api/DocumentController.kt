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
    fun refreshDocuments() {
        val topLevelFolder = formatFolder(config["documents.location"])

        val allDocs = repo.getAllFlat()

        val unrefreshedDocs = allDocs.filter{it.isFile}.toMutableList()
        val unrefreshedDirs = allDocs.filter{!it.isFile}.toMutableList()

        val topLevelDir = unrefreshedDirs.find{it.path == topLevelFolder}
        if ( topLevelDir == null)
        {
            repo.add(topLevelFolder, displayName(topLevelFolder), false, null)
        }
        else
        {
            unrefreshedDirs.remove(topLevelDir)
        }
        refreshDocumentsInDir(topLevelFolder,  unrefreshedDocs, unrefreshedDirs)

        //Hide all known files and folders which were not found
        unrefreshedDocs.forEach{ repo.setVisibility(it, false) }
        unrefreshedDirs.forEach{ repo.setVisibility(it, false) }
    }

    private fun refreshDocumentsInDir(path: String,
                                      unrefreshedDocs: MutableList<Document>,
                                      unrefreshedDirs: MutableList<Document>)
    {
        val files = fileSystem.getChildFiles(path)
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
                repo.add(file, displayName(file), true, path)
            }
        }

        val folders = fileSystem.getChildFolders(path)
        for(folderPath in folders)
        {
            val folder = formatFolder(folderPath)
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
                repo.add(folder, displayName(folder), false, path)
            }

            refreshDocumentsInDir(folder, unrefreshedDocs, unrefreshedDirs)
        }
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

    private fun displayName(path: String): String
    {
        val result = path.split("/").last()
        return result.removeSuffix("/")
    }

}