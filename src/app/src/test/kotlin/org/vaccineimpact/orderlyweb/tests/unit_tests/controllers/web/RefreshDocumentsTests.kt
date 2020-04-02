package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.DocumentDetails
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.controllers.web.DocumentController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.repositories.DocumentRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.errors.InvalidOperationError
import org.vaccineimpact.orderlyweb.models.Document
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest
import java.net.URL
import java.util.zip.ZipException

class RefreshDocumentsTests : ControllerTest()
{
    private val mockConfig = mock<Config> {
        on { get("documents.root") } doReturn "documents"
    }

    private val mockContext = mock<ActionContext> {
        on { postData<String>("url") } doReturn "http://url.com"
    }

    private val mockRepo = mock<DocumentRepository> {
        on { getAllFlat() } doReturn listOf<Document>()
    }

    private val mockFiles = mock<FileSystem> {
        on { getAbsolutePath("documents") } doReturn ("/documents")
        on { getAllChildren(any(), any()) } doReturn listOf<DocumentDetails>()
    }

    @Test
    fun `refreshDocuments downloads and saves files from url`()
    {
        val sut = DocumentController(mockContext, mockConfig, mockFiles, mockRepo)
        sut.refreshDocuments()
        verify(mockFiles).save(URL("http://url.com"), "/documents")
    }

    @Test
    fun `refreshDocuments ensures dropbox urls are downloadable`()
    {
        val mockContext = mock<ActionContext> {
            on { postData<String>("url") } doReturn "http://dropbox.com?dl=0"
        }

        val sut = DocumentController(mockContext, mockConfig, mockFiles, mockRepo)
        sut.refreshDocuments()
        verify(mockFiles).save(URL("http://dropbox.com?dl=1"), "/documents")
    }

    @Test
    fun `refreshDocuments populates all docs when there are none pre-existing`()
    {
        val mockFiles = mock<FileSystem> {
            on { getAbsolutePath("documents") } doReturn ("/documents")

            on { getAllChildren("/documents", "/documents") } doReturn listOf(
                    DocumentDetails("child1", "child1 display", "/documents/child1", "/child1", false, false),
                    DocumentDetails("child2", "child2 display", "/documents/child2", "/child2", false, false),
                    DocumentDetails("rootFile1.csv", "rootFile display", "/documents/rootFile1.csv", "/rootFile1.csv", true, false)
            )

            on { getAllChildren("/documents/child1", "/documents") } doReturn listOf(
                    DocumentDetails("grandchild", "grandchild display", "/documents/child1/grandchild", "/child1/grandchild", false, false)
            )
            on { getAllChildren("/documents/child2", "/documents") } doReturn listOf(
                    DocumentDetails("http://external.com", "external display", "http://external.com", "http://external.com", true, true)
            )

            on { getAllChildren("/documents/child1/grandchild", "/documents") } doReturn listOf(
                    DocumentDetails("grandchildFile1.csv", "grandchildFile1 display", "/documents/child1/grandchild/grandchildFile1.csv",
                            "/child1/grandchild/grandchildFile1.csv", true, false)
            )
        }

        val sut = DocumentController(mockContext, mockConfig, mockFiles, mockRepo)
        sut.refreshDocuments()

        //Expect create
        verify(mockRepo).add("/child1", "child1", "child1 display", false, false, null)
        verify(mockRepo).add("/child2", "child2", "child2 display", false, false, null)
        verify(mockRepo).add("http://external.com", "http://external.com", "external display", true, true, "/child2")
        verify(mockRepo).add("/rootFile1.csv", "rootFile1.csv", "rootFile display", true, false, null)
        verify(mockRepo).add("/child1/grandchild", "grandchild", "grandchild display", false, false, "/child1")
        verify(mockRepo).add("/child1/grandchild/grandchildFile1.csv", "grandchildFile1.csv", "grandchildFile1 display", true, false, "/child1/grandchild")
    }

    @Test
    fun `refreshDocuments refreshes show field of existing documents`()
    {
        val mockFiles = mock<FileSystem> {
            on { getAbsolutePath("documents") } doReturn ("/documents")

            on { getAllChildren("/documents", "/documents") } doReturn listOf(
                    DocumentDetails("stillExists", "stillExists display", "/documents/stillExists", "/stillExists", false, false),
                    DocumentDetails("reAdded", "reAdded display", "/documents/reAdded", "/reAdded", false, false),
                    DocumentDetails("stillExists.csv", "stillExists display", "/documents/stillExists.csv", "/stillExists.csv", true, false),
                    DocumentDetails("reAdded.csv", "reAdded display", "/documents/reAdded.csv", "/reAdded.csv", true, false)
            )

            on { getAllChildren("/documents/stillExists", "/documents") } doReturn listOf<DocumentDetails>()
            on { getAllChildren("/documents/reAdded", "/documents") } doReturn listOf<DocumentDetails>()
        }

        val flatDocs = listOf(
                Document("stillExists.csv", "stillExists display", "/stillExists.csv", true, false, listOf()),
                Document("deleted.csv", "deleted display", "/deleted.csv", true, false, listOf()),
                Document("reAdded.csv", "reAdded display", "/reAdded.csv", true, false, listOf()),

                Document("stillExists", "stillExists display", "/stillExists", false, false, listOf()),
                Document("deleted", "deleted display", "/deleted", false, false, listOf()),
                Document("reAdded", "reAdded display", "/reAdded", false, false, listOf())
        )
        val mockRepo = mock<DocumentRepository> {
            on { getAllFlat() } doReturn flatDocs
        }

        val sut = DocumentController(mockContext, mockConfig, mockFiles, mockRepo)
        sut.refreshDocuments()

        verify(mockRepo).setVisibility(listOf(flatDocs[0]), true) // still exists
        verify(mockRepo).setVisibility(listOf(flatDocs[2]), true) //re-added file
        verify(mockRepo).setVisibility(listOf(flatDocs[3]), true) //still exists
        verify(mockRepo).setVisibility(listOf(flatDocs[5]), true) //re-added folder

        verify(mockRepo).setVisibility(listOf(flatDocs[1], flatDocs[4]), false) //deleted folder

        verify(mockRepo, times(0)).add(any(), any(), any(), any(), any(), any())

    }

    @Test
    fun `refreshDocuments throws error if url is invalid`()
    {
        val mockContext = mock<ActionContext> {
            on { postData<String>("url") } doReturn "badurl"
        }
        val sut = DocumentController(mockContext, mockConfig, mockFiles, mockRepo)
        assertThatThrownBy { sut.refreshDocuments() }
                .isInstanceOf(BadRequest::class.java)
    }

    @Test
    fun `refreshDocuments throws error if zip is invalid`()
    {
        val mockFiles = mock<FileSystem> {
            on { getAbsolutePath("documents") } doReturn ("/documents")
            on { save(any(), any()) } doThrow ZipException()
        }
        val sut = DocumentController(mockContext, mockConfig, mockFiles, mockRepo)
        assertThatThrownBy { sut.refreshDocuments() }
                .isInstanceOf(InvalidOperationError::class.java)
    }
}