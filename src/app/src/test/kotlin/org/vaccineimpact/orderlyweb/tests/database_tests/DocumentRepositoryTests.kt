package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.Document
import org.vaccineimpact.orderlyweb.db.OrderlyDocumentRepository
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertDocuments

class DocumentRepositoryTests : CleanDatabaseTests() {

    @Test
    fun `can build document tree`() {

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
}
