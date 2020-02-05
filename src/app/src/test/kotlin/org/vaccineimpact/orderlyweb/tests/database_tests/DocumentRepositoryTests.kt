package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.OrderlyDocumentRepository
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.Document
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests

class DocumentRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can build document tree`()
    {

        insertDocuments()
        val sut = OrderlyDocumentRepository()
        val result = sut.getAll()
        val expectedLeaf1 = Document("first.csv", "/some/first.csv", true, listOf())
        val expectedLeaf2 = Document("file.csv", "/some/path/file.csv", true, listOf())
        val expectedLeaf3 = Document("empty", "/some/empty/", false, listOf())
        val expectedRoot1 = Document("root", "/root/", false, listOf())
        val expectedRoot2 = Document("some", "/some/", false,
                listOf(expectedLeaf3, expectedLeaf1, Document("path", "/some/path/", false, listOf(expectedLeaf2))))

        assertThat(result.count()).isEqualTo(2)
        assertThat(result.first()).isEqualTo(expectedRoot1)
        assertThat(result.last()).isEqualTo(expectedRoot2)
    }

    private fun insertDocuments()
    {
        JooqContext().use {

            it.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
                    .set(Tables.ORDERLYWEB_DOCUMENT.NAME, "some")
                    .set(Tables.ORDERLYWEB_DOCUMENT.PATH, "/some/")
                    .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, 0)
                    .execute()

            it.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
                    .set(Tables.ORDERLYWEB_DOCUMENT.NAME, "root")
                    .set(Tables.ORDERLYWEB_DOCUMENT.PATH, "/root/")
                    .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, 0)
                    .execute()

            it.dsl.newRecord(Tables.ORDERLYWEB_DOCUMENT)
                    .apply {
                        this.name = "path"
                        this.path = "/some/path/"
                        this.parent = "/some/"
                        this.isFile = 0
                    }.insert()

            it.dsl.newRecord(Tables.ORDERLYWEB_DOCUMENT)
                    .apply {
                        this.name = "file.csv"
                        this.path = "/some/path/file.csv"
                        this.parent = "/some/path/"
                    }.insert()

            it.dsl.newRecord(Tables.ORDERLYWEB_DOCUMENT)
                    .apply {
                        this.name = "first.csv"
                        this.path = "/some/first.csv"
                        this.parent = "/some/"
                    }.insert()

            it.dsl.newRecord(Tables.ORDERLYWEB_DOCUMENT)
                    .apply {
                        this.name = "empty"
                        this.path = "/some/empty/"
                        this.parent = "/some/"
                        this.isFile = 0
                    }.insert()
        }
    }
}
