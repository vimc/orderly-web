package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.After
import org.junit.Test
import org.mockito.internal.verification.Times
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.controllers.web.DocumentController
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.DocumentRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError
import org.vaccineimpact.orderlyweb.models.Document
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import java.io.File

class DocumentControllerTests : ControllerTest()
{
    @After
    fun cleanup()
    {
        File("documents").deleteRecursively()
    }

    @Test
    fun `can get document`()
    {
        File("documents/some/path").mkdirs()
        File("documents/some/path/file.csv").createNewFile()

        val mockContext = mock<ActionContext>() {
            on { splat() } doReturn arrayOf("some", "path", "file.csv")
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }

        val sut = DocumentController(mockContext, AppConfig(), Files(), mock())
        sut.getDocument()
        verify(mockContext)
                .addResponseHeader("Content-Disposition", "attachment; filename=\"some/path/file.csv\"")
        verify(mockContext).addDefaultResponseHeaders("text/csv")
    }

    @Test
    fun `attachment header is not added for inline documents`()
    {
        File("documents/some/path").mkdirs()
        File("documents/some/path/file.csv").createNewFile()

        val mockContext = mock<ActionContext>() {
            on { splat() } doReturn arrayOf("some", "path", "file.csv")
            on { queryParams("inline") } doReturn "true"
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }

        val sut = DocumentController(mockContext, AppConfig(), Files(), mock())
        sut.getDocument()
        verify(mockContext, Times(0))
                .addResponseHeader(eq("Content-Disposition"), any())
    }

    @Test
    fun `error is thrown if the path is missing`()
    {
        val sut = DocumentController(mock(), AppConfig(), Files(), mock())
        assertThatThrownBy { sut.getDocument() }.isInstanceOf(MissingParameterError::class.java)
    }

    @Test
    fun `error is thrown if the file does not exist`()
    {
        val mockContext = mock<ActionContext>() {
            on { splat() } doReturn arrayOf("some", "path", "file.csv")
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }

        val sut = DocumentController(mockContext, AppConfig(), Files(), mock())
        assertThatThrownBy { sut.getDocument() }.isInstanceOf(OrderlyFileNotFoundError::class.java)
    }

    @Test
    fun `creates correct breadcrumbs`()
    {
        val mockRepo = mock<DocumentRepository> {
            on { getAllVisibleDocuments() } doReturn listOf(Document("name", "display name", "path", true, false, listOf()))
        }
        val sut = DocumentController(mock(), AppConfig(), Files(), mockRepo)
        val result = sut.getAll()
        assertThat(result.breadcrumbs[0]).isEqualToComparingFieldByField(IndexViewModel.breadcrumb)
        assertThat(result.breadcrumbs[1].name).isEqualTo("Project documentation")
        assertThat(result.breadcrumbs[1].url).isEqualTo("http://localhost:8888/project-docs")
    }

    @Test
    fun `creates document viewmodels`()
    {
        val mockRepo = mock<DocumentRepository> {
            on { getAllVisibleDocuments() } doReturn
                    listOf(Document("name", "displayName", "/path", false, false, listOf(
                            Document("child", "child display name", "/childpath", true, false, listOf()),
                            Document("www.externalchild.com", "external display name", "/childpath.url", true, true, listOf())

                    )))
        }
        val sut = DocumentController(mock(), AppConfig(), Files(), mockRepo)
        val result = sut.getAll()

        assertThat(result.docs[0].displayName).isEqualTo("displayName")
        assertThat(result.docs[0].path).isEqualTo("/path")
        assertThat(result.docs[0].isFile).isFalse()
        assertThat(result.docs[0].external).isFalse()
        assertThat(result.docs[0].url).isEqualTo("http://localhost:8888/project-docs/path")
        assertThat(result.docs[0].children.count()).isEqualTo(2)

        var child = result.docs[0].children[0]
        assertThat(child.displayName).isEqualTo("child display name")
        assertThat(child.path).isEqualTo("/childpath")
        assertThat(child.isFile).isTrue()
        assertThat(child.external).isFalse()
        assertThat(child.url).isEqualTo("http://localhost:8888/project-docs/childpath")
        assertThat(child.children.count()).isEqualTo(0)

        child = result.docs[0].children[1]
        assertThat(child.displayName).isEqualTo("external display name")
        assertThat(child.path).isEqualTo("/childpath.url")
        assertThat(child.isFile).isTrue()
        assertThat(child.external).isTrue()
        assertThat(child.url).isEqualTo("www.externalchild.com")
        assertThat(child.children.count()).isEqualTo(0)
    }

    @Test
    fun `sets canManage from context`()
    {
        val mockRepo = mock<DocumentRepository> {
            on { getAllVisibleDocuments() } doReturn listOf<Document>()
        }
        val hasPermContext = mock<ActionContext> {
            on { hasPermission(ReifiedPermission("documents.manage", Scope.Global()))} doReturn true
        }
        val noPermContext = mock<ActionContext> {
            on { hasPermission(ReifiedPermission("documents.manage", Scope.Global()))} doReturn false
        }
        var sut = DocumentController(hasPermContext, AppConfig(), Files(), mockRepo)
        var result = sut.getAll()

        assertThat(result.canManage).isTrue()

        sut = DocumentController(noPermContext, AppConfig(), Files(), mockRepo)
        result = sut.getAll()

        assertThat(result.canManage).isFalse()
    }

    @Test
    fun `sets canOpen correctly`()
    {
        val mockRepo = mock<DocumentRepository> {
            on { getAllVisibleDocuments() } doReturn
                    listOf(Document("name", "display name", "/path", false, false, listOf(
                            Document("image", "image display name", "/image.png", true, false, listOf()),
                            Document("external", "external display name", "www.external.com", true, true, listOf()),
                            Document("table", "table display name", "/table.csv", true, false, listOf())
                    )))
        }
        val sut = DocumentController(mock(), AppConfig(), Files(), mockRepo)
        val result = sut.getAll()
        assertThat(result.docs[0].displayName).isEqualTo("display name")
        assertThat(result.docs[0].canOpen).isFalse()

        val children = result.docs[0].children
        assertThat(children[0].displayName).isEqualTo("image display name")
        assertThat(children[0].canOpen).isTrue()
        assertThat(children[1].displayName).isEqualTo("external display name")
        assertThat(children[1].canOpen).isTrue()
        assertThat(children[2].displayName).isEqualTo("table display name")
        assertThat(children[2].canOpen).isFalse()
    }

    @Test
    fun `does not include empty folders`()
    {
        val mockRepo = mock<DocumentRepository> {
            on { getAllVisibleDocuments() } doReturn
                    listOf(Document("toplevelwithonenonemptychild", "toplevelwithonenonemptychild display name", "toplevelwithonenonemptychild", false, false, listOf(
                            Document("emptychild", "emptychild display name", "emptychild", false, false, listOf()),
                            Document("file", "file display name", "file", true, false, listOf())
                    )),
                            Document("toplevelempty", "toplevelempty display name", "toplevelempty", false, false, listOf()),
                            Document("toplevelwithemptychild", "toplevelwithemptychild display name", "toplevelwithemptychild", false, false,
                                    listOf(Document("emptychild", "emptychild display name", "emptychild", false, false, listOf()))),
                            Document("toplevelwithemptygrandchild", "toplevelwithemptygrandchild display name", "toplevelwithemptygrandchild", false, false,
                                    listOf(Document("childwithemptygrandchild", "childwithemptygrandchild display name", "childwithemptygrandchild", false, false, listOf(
                                            Document("grandchildempty", "grandchildempty display name", "grandchildempty", false, false, listOf())
                                    )))))
        }
        val sut = DocumentController(mock(), AppConfig(), Files(), mockRepo)
        val result = sut.getAll()
        assertThat(result.docs.count()).isEqualTo(1)
        val doc = result.docs[0]
        assertThat(doc.displayName).isEqualTo("toplevelwithonenonemptychild display name")
        assertThat(doc.children.count()).isEqualTo(1)
        assertThat(doc.children[0].displayName).isEqualTo("file display name")
    }

    @Test
    fun `orders folders first, then alphabetically`()
    {
        val mockRepo = mock<DocumentRepository> {
            on { getAllVisibleDocuments() } doReturn
                    listOf(Document("folder", "folder display name", "path", false, false,
                            listOf(Document("file", "file display name", "path", true, false, listOf()))),
                            Document("file", "file display name", "path", true, false, listOf()),
                            Document("anotherfolder", "anotherfolder display name", "path", false, false,
                                    listOf(Document("file", "file display name", "path", true, false, listOf()))),
                            Document("anotherfile", "anotherfile display name", "path", true, false, listOf()))
        }
        val sut = DocumentController(mock(), AppConfig(), Files(), mockRepo)
        val result = sut.getAll()
        assertThat(result.docs[0].displayName).isEqualTo("anotherfolder display name")
        assertThat(result.docs[1].displayName).isEqualTo("folder display name")
        assertThat(result.docs[2].displayName).isEqualTo("anotherfile display name")
        assertThat(result.docs[3].displayName).isEqualTo("file display name")
    }

}