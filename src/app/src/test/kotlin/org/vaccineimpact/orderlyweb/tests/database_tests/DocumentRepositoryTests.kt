package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyDocumentRepository
import org.vaccineimpact.orderlyweb.models.Document
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests

class DocumentRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can build document tree`()
    {
        insertDocuments()
        val sut = OrderlyDocumentRepository()
        val result = sut.getAllVisibleDocuments()
        val expectedLeaf1 = Document("first.csv", "first.csv", "/some/first.csv", true, false, listOf())
        val expectedLeaf2 = Document("file.csv", "file.csv", "/some/path/file.csv", true, false, listOf())
        val expectedLeaf3 = Document("empty", "empty", "/some/empty/", false, false, listOf())
        val expectedRoot1 = Document("root", "root", "/root/", false, false, listOf())
        val expectedRoot2 = Document("some", "some display", "/some/", false, false,
                listOf(expectedLeaf3, expectedLeaf1, Document("path", "path", "/some/path/", false, false, listOf(expectedLeaf2)),
                        Document("http://external.com", "http://external.com", "/some/external.web.url", true, true, listOf())))

        assertThat(result.count()).isEqualTo(2)
        assertThat(result.first()).isEqualTo(expectedRoot1)
        assertThat(result.last()).isEqualTo(expectedRoot2)
    }

    @Test
    fun `does not return documents with show = false`()
    {
        JooqContext().use {
            it.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
                    .set(Tables.ORDERLYWEB_DOCUMENT.NAME, "some")
                    .set(Tables.ORDERLYWEB_DOCUMENT.PATH, "/some/")
                    .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, 0)
                    .set(Tables.ORDERLYWEB_DOCUMENT.SHOW, 0)
                    .execute()

            it.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
                    .set(Tables.ORDERLYWEB_DOCUMENT.NAME, "root")
                    .set(Tables.ORDERLYWEB_DOCUMENT.PATH, "/root/")
                    .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, 0)
                    .set(Tables.ORDERLYWEB_DOCUMENT.SHOW, 1)
                    .execute()

            it.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
                    .set(Tables.ORDERLYWEB_DOCUMENT.NAME, "file.csv")
                    .set(Tables.ORDERLYWEB_DOCUMENT.PATH, "/root/file.csv")
                    .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, 0)
                    .set(Tables.ORDERLYWEB_DOCUMENT.PARENT, "/root/")
                    .set(Tables.ORDERLYWEB_DOCUMENT.SHOW, 0)
                    .execute()
        }

        val sut = OrderlyDocumentRepository()
        val result = sut.getAllVisibleDocuments()
        assertThat(result.count()).isEqualTo(1)
        assertThat(result.first().displayName).isEqualTo("root")
    }

    @Test
    fun `can get flat list of documents`()
    {
        insertDocuments()
        val sut = OrderlyDocumentRepository()
        val result = sut.getAllFlat().sortedBy { it.path }
        assertThat(result.count()).isEqualTo(7)
        assertThat(result[0]).isEqualTo(Document("root", "root", "/root/", false, false, listOf()))
        assertThat(result[1]).isEqualTo(Document("some", "some display", "/some/", false, false, listOf()))
        assertThat(result[2]).isEqualTo(Document("empty", "empty", "/some/empty/", false, false, listOf()))
        assertThat(result[3]).isEqualTo(Document("http://external.com", "http://external.com", "/some/external.web.url", true, true, listOf()))
        assertThat(result[4]).isEqualTo(Document("first.csv", "first.csv", "/some/first.csv", true, false, listOf()))
        assertThat(result[5]).isEqualTo(Document("path", "path", "/some/path/", false, false, listOf()))
        assertThat(result[6]).isEqualTo(Document("file.csv", "file.csv", "/some/path/file.csv", true, false, listOf()))
    }

    @Test
    fun `can add documents`()
    {
        val sut = OrderlyDocumentRepository()
        sut.add("/root/", "root", "root", false, false, null)
        sut.add("/root/file.csv", "file.csv", "file display", true, false, "/root/")
        sut.add("/root/some.web.url", "http://external.com", "external display", true, true, "/root/")

        JooqContext().use {
            val result = it.dsl.selectFrom(Tables.ORDERLYWEB_DOCUMENT)
                    .orderBy(Tables.ORDERLYWEB_DOCUMENT.PATH)
                    .fetch()

            assertThat(result.count()).isEqualTo(3)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/root/")
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.DISPLAY_NAME]).isEqualTo("root")
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.DESCRIPTION]).isEqualTo(null)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("root")
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.EXTERNAL]).isEqualTo(0)
            assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo(null)

            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/root/file.csv")
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.DISPLAY_NAME]).isEqualTo("file display")
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.DESCRIPTION]).isEqualTo(null)
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("file.csv")
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.EXTERNAL]).isEqualTo(0)
            assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/root/")

            assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/root/some.web.url")
            assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.DISPLAY_NAME]).isEqualTo("external display")
            assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.DESCRIPTION]).isEqualTo(null)
            assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("http://external.com")
            assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)
            assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.EXTERNAL]).isEqualTo(1)
            assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/root/")
        }
    }

    @Test
    fun `can set document visibility`()
    {
        insertDocuments()
        val sut = OrderlyDocumentRepository()

        val document1 = sut.getAllFlat().filter { it.path == "/some/" }[0]
        val document2 = sut.getAllFlat().filter { it.path == "/some/path/" }[0]

        JooqContext().use {
            val query = it.dsl.select(Tables.ORDERLYWEB_DOCUMENT.SHOW)
                    .from(Tables.ORDERLYWEB_DOCUMENT)
                    .where(Tables.ORDERLYWEB_DOCUMENT.PATH.`in`(document1.path, document2.path))

            var show = query.fetch().map { it[Tables.ORDERLYWEB_DOCUMENT.SHOW] }
            assertThat(show[0]).isEqualTo(1)
            assertThat(show[1]).isEqualTo(1)

            sut.setVisibility(listOf(document1, document2), false)

            show = query.fetch().map { it[Tables.ORDERLYWEB_DOCUMENT.SHOW] }
            assertThat(show[0]).isEqualTo(0)
            assertThat(show[1]).isEqualTo(0)

            sut.setVisibility(listOf(document1, document2), true)

            show = query.fetch().map { it[Tables.ORDERLYWEB_DOCUMENT.SHOW] }
            assertThat(show[0]).isEqualTo(1)
            assertThat(show[1]).isEqualTo(1)
        }
    }

    private fun insertDocuments()
    {
        JooqContext().use {

            it.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
                    .set(Tables.ORDERLYWEB_DOCUMENT.NAME, "some")
                    .set(Tables.ORDERLYWEB_DOCUMENT.DISPLAY_NAME, "some display")
                    .set(Tables.ORDERLYWEB_DOCUMENT.PATH, "/some/")
                    .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, 0)
                    .set(Tables.ORDERLYWEB_DOCUMENT.SHOW, 1)
                    .execute()

            it.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
                    .set(Tables.ORDERLYWEB_DOCUMENT.NAME, "root")
                    .set(Tables.ORDERLYWEB_DOCUMENT.DISPLAY_NAME, "root")
                    .set(Tables.ORDERLYWEB_DOCUMENT.PATH, "/root/")
                    .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, 0)
                    .set(Tables.ORDERLYWEB_DOCUMENT.SHOW, 1)
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

            it.dsl.newRecord(Tables.ORDERLYWEB_DOCUMENT)
                    .apply {
                        this.name = "http://external.com"
                        this.path = "/some/external.web.url"
                        this.parent = "/some/"
                        this.isFile = 1
                        this.external = 1
                        this.show = 1
                    }.insert()
        }
    }
}
