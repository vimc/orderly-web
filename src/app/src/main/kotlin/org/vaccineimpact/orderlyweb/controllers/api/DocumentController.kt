package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.DocumentRepository
import org.vaccineimpact.orderlyweb.models.documents.Directory
import org.vaccineimpact.orderlyweb.models.documents.Document

class DocumentController(context: ActionContext,
                         private val fileSystem: FileSystem = Files(),
                         private val config: Config = AppConfig(),
                         private val repo: DocumentRepository): Controller(context)
{
    fun refreshDocuments() {
        val topLevelFolder = trimFolder(config["documents.location"])

        val unrefreshedDocs = repo.getAllDocuments().toMutableList()
        val unrefreshedDirs = repo.getAllDirectories().toMutableList()

        val topLevelDir = unrefreshedDirs.find{it.path == topLevelFolder}
        if ( topLevelDir == null)
        {
            repo.addDirectory(topLevelFolder, folderName(topLevelFolder), null)
        }
        else
        {
            unrefreshedDirs.remove(topLevelDir)
        }
        refreshDocumentsInDir(topLevelFolder,  unrefreshedDocs, unrefreshedDirs)

        //Hide all known files and folders which were not found
        unrefreshedDocs.forEach{ repo.setDocumentVisibility(it, false) }
        unrefreshedDirs.forEach{ repo.setDirectoryVisibility(it, false) }
    }

    private fun refreshDocumentsInDir(path: String,
                                      unrefreshedDocs: MutableList<Document>,
                                      unrefreshedDirs: MutableList<Directory>)
    {
        val files = fileSystem.getChildFiles(path)
        for(file in files)
        {
            val documentForFile = unrefreshedDocs.find{it.dirPath == path && it.filename == file}

            if (documentForFile != null)
            {
                if (!documentForFile.show)
                {
                    repo.setDocumentVisibility(documentForFile, true)
                }
               unrefreshedDocs.remove(documentForFile)
            }
            else
            {
                repo.addDocument(file, path)
            }
        }

        val folders = fileSystem.getChildFolders(path)
        for(folderPath in folders)
        {
            val folder = trimFolder(folderPath)
            val dirForFolder = unrefreshedDirs.find{it.path == folder}
            if (dirForFolder != null)
            {
                if (!dirForFolder.show)
                {
                    repo.setDirectoryVisibility(dirForFolder, true)
                }
                unrefreshedDirs.remove(dirForFolder)
            }
            else
            {
                repo.addDirectory(folder, folderName(folder), path)
            }

            refreshDocumentsInDir(folder, unrefreshedDocs, unrefreshedDirs)
        }
    }

    private fun trimFolder(folder: String): String
    {
        return folder.removeSuffix("/")
    }

    private fun folderName(absolutePath: String): String
    {
        return absolutePath.split("/").last()
    }
}