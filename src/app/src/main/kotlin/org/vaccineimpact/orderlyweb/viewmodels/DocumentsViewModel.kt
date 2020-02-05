package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.Document

data class DocumentsViewModel(val docs: List<Document>, val appViewModel: AppViewModel) : AppViewModel by appViewModel
{
    companion object
    {
        val breadcrumb = Breadcrumb("Docs", "${AppConfig()["app.url"]}/docs")

        fun build(context: ActionContext, docs: List<Document>): DocumentsViewModel {
            return DocumentsViewModel(docs, DefaultViewModel(context, IndexViewModel.breadcrumb, breadcrumb))
        }
    }
}