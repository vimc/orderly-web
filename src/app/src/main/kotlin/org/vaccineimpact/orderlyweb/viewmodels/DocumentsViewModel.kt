package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.canRenderInBrowser
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.models.Document
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

data class DocumentViewModel(val displayName: String,
                             val path: String,
                             val url: String,
                             val isFile: Boolean,
                             val children: List<DocumentViewModel>,
                             val canOpen: Boolean,
                             val external: Boolean)
{
    companion object
    {
        fun build(doc: Document, appConfig: Config = AppConfig()): DocumentViewModel?
        {
            val url = if (doc.external)
            {
                doc.name
            }
            else
            {
                "${appConfig["app.url"]}/project-docs${doc.path}"
            }

            val canOpen = doc.file && (canRenderInBrowser(doc.path) || doc.external)

            val vm = DocumentViewModel(doc.displayName,
                    doc.path,
                    url,
                    doc.file,
                    doc.children.map { build(it) }.filterNotNull(),
                    canOpen,
                    doc.external)

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
                              val canManage: Boolean,
                              val appViewModel: AppViewModel) : AppViewModel by appViewModel
{
    companion object
    {
        val breadcrumb = Breadcrumb("Project documentation", "${AppConfig()["app.url"]}/project-docs")

        fun build(context: ActionContext, docs: List<Document>): DocumentsViewModel
        {
            val docVms = buildDocs(docs)

            return DocumentsViewModel(docVms,
                    context.hasPermission(ReifiedPermission("documents.manage", Scope.Global())),
                    DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))
        }

        fun buildDocs(docs: List<Document>): List<DocumentViewModel>
        {
            return docs
                    .map { DocumentViewModel.build(it) }
                    .filterNotNull()
                    .sortedBy { it.displayName }
                    .sortedBy { it.isFile }
        }
    }
}