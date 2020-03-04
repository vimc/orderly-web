package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.canRenderInBrowser
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.models.Document

data class DocumentViewModel(val displayName: String,
                             val path: String,
                             val url: String,
                             val isFile: Boolean,
                             val children: List<DocumentViewModel>,
                             val canOpen: Boolean)
{
    companion object
    {
        fun build(doc: Document, appConfig: Config = AppConfig()): DocumentViewModel?
        {
            val url = if (doc.external)
            {
                // displayName = name for now
                // once actual display names are implemented name
                // should be added to the document model and this
                // should be doc.name
                doc.displayName
            }
            else
            {
                "${appConfig["app.url"]}/project-docs${doc.path}"
            }

            val canOpen = doc.file && canRenderInBrowser(doc.path)

            val vm = DocumentViewModel(doc.displayName,
                    doc.path,
                    url,
                    doc.file,
                    doc.children.map { build(it) }.filterNotNull(),
                    canOpen)

            return if (vm.isFile || vm.children.any())
            {
                vm
            }
            else
            {
                null
            }
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
            val docVms = docs
                    .map { DocumentViewModel.build(it) }
                    .filterNotNull()
                    .sortedBy { it.displayName }
                    .sortedBy { it.isFile }

            return DocumentsViewModel(docVms,
                    DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))
        }
    }
}