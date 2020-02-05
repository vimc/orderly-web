package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_DOCUMENT

data class Document(val displayName: String, val path: String, val isFile: Boolean, val children: List<Document>)
interface DocumentRepository {
    fun getAll(): List<Document>
}

class OrderlyDocumentRepository : DocumentRepository {

    override fun getAll(): List<Document> {
        JooqContext().use {
            db ->
            val rootNodes = db.dsl.selectFrom(ORDERLYWEB_DOCUMENT)
                    .where(ORDERLYWEB_DOCUMENT.PARENT.isNull)
                    .fetch()
                    .sortAsc(ORDERLYWEB_DOCUMENT.NAME)
            return rootNodes.map {
                mapDocument(db, it)
            }
        }
    }

    private fun getChildren(db: JooqContext, node: Document): List<Document> {

        return db.dsl.selectFrom(ORDERLYWEB_DOCUMENT)
                .where(ORDERLYWEB_DOCUMENT.PARENT.eq(node.path))
                .fetch()
                .sortAsc(ORDERLYWEB_DOCUMENT.NAME).map {
                    mapDocument(db, it)
                }
    }

    private fun mapDocument(db: JooqContext, record: Record) : Document {
        val doc = Document(record[ORDERLYWEB_DOCUMENT.DISPLAY_NAME]?: record[ORDERLYWEB_DOCUMENT.NAME], record[ORDERLYWEB_DOCUMENT.PATH],
                record[ORDERLYWEB_DOCUMENT.IS_FILE] == 1, listOf())
        return doc.copy(children = getChildren(db, doc))
    }

}
