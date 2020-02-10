package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Test
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.controllers.api.DocumentController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.DocumentRepository
import org.vaccineimpact.orderlyweb.models.Document
import java.io.File

class DocumentControllerTests : ControllerTest()
{
    @After
    fun cleanup()
    {
        File("documents").deleteRecursively()
    }

    @Test
    fun `refreshDocuments populates all docs when there are none pre-existing`()
    {
        val mockConfig = mock<Config> {
            on { get("documents.root") } doReturn "documents"
        }

        val mockRepo = mock<DocumentRepository> {
            on { getAllFlat() } doReturn listOf<Document>()
        }

        File("documents/child1/grandchild").mkdirs()
        File("documents/child2").mkdirs()
        File("documents/child1/grandchild/grandchildFile1.csv").createNewFile()
        File("documents/rootFile1.csv").createNewFile()

        val sut = DocumentController(mock(), Files(), mockConfig, mockRepo)
        sut.refreshDocuments()

        //Expect create
        verify(mockRepo).add("/child1", "child1", false, null)
        verify(mockRepo).add("/child2", "child2", false, null)
        verify(mockRepo).add("/rootFile1.csv", "rootFile1.csv", true, null)
        verify(mockRepo).add("/child1/grandchild", "grandchild", false, "/child1")
        verify(mockRepo).add("/child1/grandchild/grandchildFile1.csv", "grandchildFile1.csv", true, "/child1/grandchild")
    }

    @Test
    fun `refreshDocuments refreshes show field of existing documents`()
    {
        val mockConfig = mock<Config> {
            on { get("documents.root") } doReturn "documents"
        }

        File("documents/stillExists").mkdirs()
        File("documents/reAdded").mkdirs()
        File("documents/stillExists.csv").createNewFile()
        File("documents/reAdded.csv").createNewFile()

        val flatDocs = listOf(
                Document("stillExists.csv", "/stillExists.csv", true, listOf()),
                Document("deleted.csv", "/deleted.csv", true, listOf()),
                Document("reAdded.csv", "/reAdded.csv", true, listOf()),

                Document("stillExists", "/stillExists", false, listOf()),
                Document("deleted", "/deleted", false, listOf()),
                Document("reAdded", "/reAdded", false, listOf())
        )
        val mockRepo = mock<DocumentRepository> {
            on { getAllFlat() } doReturn flatDocs
        }

        val sut = DocumentController(mock(), Files(), mockConfig, mockRepo)
        sut.refreshDocuments()

        verify(mockRepo).setVisibility(flatDocs[0], true) // still exists
        verify(mockRepo).setVisibility(flatDocs[1], false) //deleted file
        verify(mockRepo).setVisibility(flatDocs[2], true) //re-added file
        verify(mockRepo).setVisibility(flatDocs[3], true) //still exists
        verify(mockRepo).setVisibility(flatDocs[4], false) //deleted folder
        verify(mockRepo).setVisibility(flatDocs[5], true) //re-added folder

        verify(mockRepo, times(0)).add(any(), any(), any(), any())

    }
}