package org.vaccineimpact.orderlyweb.db

import org.vaccineimpact.orderlyweb.models.documents.Directory
import org.vaccineimpact.orderlyweb.models.documents.Document

interface DocumentRepository
{
    fun getAllDocuments(): Array<Document>
    fun getAllDirectories(): Array<Directory>

    fun addDocument(filename: String, path: String)
    fun setDocumentVisibility(document: Document, show: Boolean)

    fun addDirectory(path: String, directoryName: String, parentPath: String?)
    fun setDirectoryVisibility(dir: Directory, show: Boolean)
}