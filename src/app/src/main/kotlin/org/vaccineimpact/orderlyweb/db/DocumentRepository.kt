package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_DOCUMENT
import org.vaccineimpact.orderlyweb.models.Document

interface DocumentRepository {
    fun getAllVisibleDocuments(): List<Document>
}

class OrderlyDocumentRepository : DocumentRepository {

    override fun getAllVisibleDocuments(): List<Document> {
        JooqContext().use {
            db ->
            val rootNodes = db.dsl.selectFrom(ORDERLYWEB_DOCUMENT)
                    .where(ORDERLYWEB_DOCUMENT.PARENT.isNull)
                    .and(ORDERLYWEB_DOCUMENT.SHOW.eq(1))
                    .fetch()
            return rootNodes.map {
                mapDocument(db, it)
            }.sortedBy { it.displayName }
        }
    }

    private fun getChildren(db: JooqContext, node: Document): List<Document> {

        return db.dsl.selectFrom(ORDERLYWEB_DOCUMENT)
                .where(ORDERLYWEB_DOCUMENT.PARENT.eq(node.path))
                .and(ORDERLYWEB_DOCUMENT.SHOW.eq(1))
                .fetch().map {
                    mapDocument(db, it)
                }.sortedBy { it.displayName }
    }

    private fun mapDocument(db: JooqContext, record: Record) : Document {
        val doc = Document(record[ORDERLYWEB_DOCUMENT.DISPLAY_NAME]?: record[ORDERLYWEB_DOCUMENT.NAME], record[ORDERLYWEB_DOCUMENT.PATH],
                record[ORDERLYWEB_DOCUMENT.IS_FILE] == 1, listOf())
        return doc.copy(children = getChildren(db, doc))
    }

}
