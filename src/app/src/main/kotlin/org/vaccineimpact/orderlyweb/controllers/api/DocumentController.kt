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
        val topLevelDir = config["documents.location"]

        val unrefreshedDocs = repo.getAllDocuments().toMutableList()
        val unrefreshedDirs = repo.getAllDirectories().toMutableList()

        refreshDocumentsInDir(topLevelDir, unrefreshedDocs, unrefreshedDirs)

        //Hide all known files and folders which were not found
    }

    private fun refreshDocumentsInDir(path: String,
                                      unrefreshedDocs: MutableList<Document>,
                                      unrefreshedDirs: MutableList<Directory>)
    {
        val files = fileSystem.getChildFiles(path)

        for(file in files)
        {
            //TODO: deal with issues of trailing slashes etc
            val documentForFile = unrefreshedDocs.find{d: Document -> d.dirPath == path && d.filename == file}

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
        //foreach folder
            //if in known folders
            //   ensure shown
            //   remove from known dirs
            //else
            //   add to repo

            //refreshDocumentsInFolder(path + folder name)
    }
}