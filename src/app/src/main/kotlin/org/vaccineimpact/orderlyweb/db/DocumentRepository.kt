package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_DOCUMENT
import org.vaccineimpact.orderlyweb.models.Document

interface DocumentRepository {
    fun getAll(): List<Document>
    fun getAllFlat(): List<Document>

    fun add(path: String, name: String, isFile: Boolean, parentPath: String?)
    fun setVisibility(document: Document, show: Boolean)
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

    override fun getAllFlat(): List<Document> {
        JooqContext().use { db ->
            val result =  db.dsl.selectFrom(ORDERLYWEB_DOCUMENT)
                    .fetch()
            return result.map{ Document(
                    it[ORDERLYWEB_DOCUMENT.DISPLAY_NAME] ?: it[ORDERLYWEB_DOCUMENT.NAME],
                    it[ORDERLYWEB_DOCUMENT.PATH],
                    it[ORDERLYWEB_DOCUMENT.IS_FILE]==1,
                    it[ORDERLYWEB_DOCUMENT.SHOW]==1,
                    listOf()
            ) }
        }
    }

    override fun add(path: String, name: String, isFile: Boolean, parentPath: String?)
    {
        JooqContext().use { db ->
            db.dsl.insertInto(ORDERLYWEB_DOCUMENT)
                    .set(ORDERLYWEB_DOCUMENT.PATH, path)
                    .set(ORDERLYWEB_DOCUMENT.NAME, name)
                    .set(ORDERLYWEB_DOCUMENT.IS_FILE, isFile.toInt())
                    .set(ORDERLYWEB_DOCUMENT.PARENT, parentPath)
                    .set(ORDERLYWEB_DOCUMENT.SHOW, 1)
                    .execute()
        }
    }

    override fun setVisibility(document: Document, show: Boolean)
    {
        JooqContext().use { db ->
            db.dsl.update(ORDERLYWEB_DOCUMENT)
                    .set(ORDERLYWEB_DOCUMENT.SHOW, show.toInt())
                    .where(ORDERLYWEB_DOCUMENT.PATH.eq(document.path))
                    .execute()
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
                record[ORDERLYWEB_DOCUMENT.IS_FILE] == 1, record[ORDERLYWEB_DOCUMENT.SHOW] == 1, listOf())
        return doc.copy(children = getChildren(db, doc))
    }

    private fun Boolean.toInt(): Int
    {
        return if (this) 1 else 0
    }
}
