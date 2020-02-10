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

    @Test
    fun `can get flat list of documents`()
    {
        insertDocuments()
        val sut = OrderlyDocumentRepository()
        val result = sut.getAllFlat().sortedBy { it.path }
        assertThat(result.count()).isEqualTo(6)
        assertThat(result[0]).isEqualTo(Document("root", "/root/", false, listOf()))
        assertThat(result[1]).isEqualTo(Document("some", "/some/", false, listOf()))
        assertThat(result[2]).isEqualTo(Document("empty", "/some/empty/", false, listOf()))
        assertThat(result[3]).isEqualTo(Document("first.csv", "/some/first.csv", true, listOf()))
        assertThat(result[4]).isEqualTo(Document("path", "/some/path/", false, listOf()))
        assertThat(result[5]).isEqualTo(Document("file.csv", "/some/path/file.csv", true, listOf()))
    }

    @Test
    fun `can add documents`()
    {
        val sut = OrderlyDocumentRepository()
        sut.add("/root/", "root", false, null)
        sut.add("/root/file.csv", "file.csv", true, "/root/")

        JooqContext().use {
            val result = it.dsl.selectFrom(Tables.ORDERLYWEB_DOCUMENT)
                    .orderBy(Tables.ORDERLYWEB_DOCUMENT.PATH)
                    .fetch()

            assertThat(result.count()).isEqualTo(2)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/root/")
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.DISPLAY_NAME]).isEqualTo(null)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.DESCRIPTION]).isEqualTo(null)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("root")
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo(null)

            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/root/file.csv")
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.DISPLAY_NAME]).isEqualTo(null)
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.DESCRIPTION]).isEqualTo(null)
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("file.csv")
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/root/")
        }

    }

    @Test
    fun `can set document visibility`()
    {
        insertDocuments()
        val sut = OrderlyDocumentRepository()

        val document = sut.getAllFlat()[0]

        JooqContext().use {
            val query = it.dsl.select(Tables.ORDERLYWEB_DOCUMENT.SHOW)
                    .from(Tables.ORDERLYWEB_DOCUMENT)
                    .where(Tables.ORDERLYWEB_DOCUMENT.PATH.eq(document.path))

            var show = query.fetchOne()[Tables.ORDERLYWEB_DOCUMENT.SHOW]
            assertThat(show).isEqualTo(1)

            sut.setVisibility(document, false)

            show = query.fetchOne()[Tables.ORDERLYWEB_DOCUMENT.SHOW]
            assertThat(show).isEqualTo(0)

            sut.setVisibility(document, true)

            show = query.fetchOne()[Tables.ORDERLYWEB_DOCUMENT.SHOW]
            assertThat(show).isEqualTo(1)
        }
    }

    private fun insertDocuments()
    {
        JooqContext().use {

            it.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
                    .set(Tables.ORDERLYWEB_DOCUMENT.NAME, "some")
                    .set(Tables.ORDERLYWEB_DOCUMENT.PATH, "/some/")
                    .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, 0)
                    .set(Tables.ORDERLYWEB_DOCUMENT.SHOW, 1)
                    .execute()

            it.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
                    .set(Tables.ORDERLYWEB_DOCUMENT.NAME, "root")
                    .set(Tables.ORDERLYWEB_DOCUMENT.PATH, "/root/")
                    .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, 0)
                    .set(Tables.ORDERLYWEB_DOCUMENT.SHOW, 0)
                    .execute()

            it.dsl.newRecord(Tables.ORDERLYWEB_DOCUMENT)
                    .apply {
                        this.name = "path"
                        this.path = "/some/path/"
                        this.parent = "/some/"
                        this.isFile = 0
                        this.show = 1
                    }.insert()

            it.dsl.newRecord(Tables.ORDERLYWEB_DOCUMENT)
                    .apply {
                        this.name = "file.csv"
                        this.path = "/some/path/file.csv"
                        this.parent = "/some/path/"
                        this.show = 1
                    }.insert()

            it.dsl.newRecord(Tables.ORDERLYWEB_DOCUMENT)
                    .apply {
                        this.name = "first.csv"
                        this.path = "/some/first.csv"
                        this.parent = "/some/"
                        this.show = 1
                    }.insert()

            it.dsl.newRecord(Tables.ORDERLYWEB_DOCUMENT)
                    .apply {
                        this.name = "empty"
                        this.path = "/some/empty/"
                        this.parent = "/some/"
                        this.isFile = 0
                        this.show = 1
                    }.insert()
        }
    }
}
