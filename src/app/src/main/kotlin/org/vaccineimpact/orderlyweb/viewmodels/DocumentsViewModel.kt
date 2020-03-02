package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.models.Document

data class DocumentViewModel(val displayName: String,
                             val path: String,
                             val url: String,
                             val isFile: Boolean,
                             val children: List<DocumentViewModel>)
{
    companion object
    {
        fun build(doc: Document, appConfig: Config = AppConfig()): DocumentViewModel
        {
            return DocumentViewModel(doc.displayName, doc.path, "${appConfig["app.url"]}/project-docs/${doc.path}",
                    doc.file, doc.children.map { build(it) })
        }
    }
}

data class DocumentsViewModel(@Serialise("documentList")
                              val docs: List<DocumentViewModel>,
                              val appViewModel: AppViewModel) : AppViewModel by appViewModel
{
    companion object
    {
        val breadcrumb = Breadcrumb("Project documentation", "${AppConfig()["app.url"]}/project-docs")

        fun build(context: ActionContext, docs: List<Document>): DocumentsViewModel
        {
            val docVms = docs.filter { it.file || it.children.any() } // don't include empty folders
                    .map { DocumentViewModel.build(it) }
                    .sortedBy { it.isFile }

            return DocumentsViewModel(docVms,
                    DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))
        }
    }
}