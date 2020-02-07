package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.controllers.api.DocumentController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.DocumentRepository
import org.vaccineimpact.orderlyweb.models.Document

class DocumentControllerTests : ControllerTest()
{
    @Test
    fun `refreshDocuments populates all docs when there are none pre-existing`()
    {
        val mockConfig = mock<Config>  {
            on { get("documents.root") } doReturn "root"
        }

        val mockRepo = mock<DocumentRepository> {
            on { getAllFlat() } doReturn listOf<Document>()
        }

        // /root/rootFile1.csv, rootFile2.csv
        // /root/child1/child1File1.csv
        // /root/child2/grandchild/grandchildFile1.csv
        val mockFiles = mock<FileSystem> {
            on { getAbsolutePath("root") } doReturn "/root"

            on { getChildFiles("/root") } doReturn listOf("/root/rootFile1.csv", "/root/rootFile2.csv")
            on { getChildFolders("/root") } doReturn listOf("/root/child1", "/root/child2")

            on { getChildFiles("/root/child1") } doReturn listOf("/root/child1/childFile1.csv")
            on { getChildFolders("/root/child1") } doReturn listOf("/root/child1/grandchild")

            on { getChildFiles("/root/child2") } doReturn listOf<String>()
            on { getChildFolders("/root/child2") } doReturn listOf<String>()

            on { getChildFiles("/root/child1/grandchild") } doReturn listOf("/root/child1/grandchild/grandchildFile1.csv")
            on { getChildFolders("/root/child1/grandchild") } doReturn listOf<String>()
        }

        val sut = DocumentController(mock(), mockFiles, mockConfig, mockRepo)
        sut.refreshDocuments()

        //Expect create
        verify(mockRepo).add("/", "root", false, null)
        verify(mockRepo).add("/rootFile1.csv", "rootFile1.csv", true, "/")
        verify(mockRepo).add("/rootFile2.csv", "rootFile2.csv", true, "/")

        verify(mockRepo).add("/child1/", "child1", false, "/")
        verify(mockRepo).add("/child1/childFile1.csv", "childFile1.csv", true, "/child1/")

        verify(mockRepo).add("/child2/", "child2", false, "/")

        verify(mockRepo).add("/child1/grandchild/", "grandchild", false, "/child1/")
        verify(mockRepo).add("/child1/grandchild/grandchildFile1.csv", "grandchildFile1.csv", true, "/child1/grandchild/")

        verify(mockRepo, times(0)).setVisibility(any(), any())
    }

    @Test
    fun `refreshDocuments refreshes show field of existing documents`()
    {
        val mockConfig = mock<Config>  {
            on { get("documents.root") } doReturn "root"
        }

        val flatDocs = listOf(
                Document("stillExists.csv", "/stillExists.csv", true, true, listOf()),
                Document("deleted.csv", "/deleted.csv", true, true, listOf()),
                Document("reAdded.csv", "/reAdded.csv", true, false, listOf()),
                Document("previouslyDeleted.csv", "/previouslyDeleted.csv", true, false, listOf()),

                Document("stillExists", "/stillExists/", false, true, listOf()),
                Document("deleted", "/deleted/", false, true, listOf()),
                Document("reAdded", "/reAdded/", false, false, listOf()),
                Document("previouslyDeleted", "/previouslyDeleted/", false, false, listOf())
        )
        val mockRepo = mock<DocumentRepository> {
            on { getAllFlat() } doReturn flatDocs
        }

        val mockFiles = mock<FileSystem> {
            on { getAbsolutePath("root") } doReturn "/root"
            on { getChildFiles("/root") } doReturn listOf("/root/stillExists.csv", "/root/reAdded.csv")
            on { getChildFolders("/root") } doReturn listOf("/root/stillExists", "/root/reAdded")

        }

        val sut = DocumentController(mock(), mockFiles, mockConfig, mockRepo)
        sut.refreshDocuments()

        verify(mockRepo).setVisibility(flatDocs[1], false) //deleted file
        verify(mockRepo).setVisibility(flatDocs[2], true) //re-added file
        verify(mockRepo).setVisibility(flatDocs[5], false) //deleted folder
        verify(mockRepo).setVisibility(flatDocs[6], true) //re-added folder

        verify(mockRepo, times(4)).setVisibility(any(), any())
        verify(mockRepo, times(0)).add(any(), any(), any(), any())

    }
}