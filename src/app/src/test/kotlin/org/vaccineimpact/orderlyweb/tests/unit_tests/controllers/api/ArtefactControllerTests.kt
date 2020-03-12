package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.controllers.api.ArtefactController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.ArtefactRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError

class ArtefactControllerTests : ControllerTest()
{
    private val mockConfig = mock<Config> {
        on { this.get("orderly.root") } doReturn "root/"
    }

    private val name = "testname"
    private val version = "testversion"

    @Test
    fun `gets artefacts for report`()
    {
        val artefacts = mapOf("test.png" to "hjkdasjkldas6762i1j")

        val repo = mock<ArtefactRepository> {
            on { this.getArtefactHashes(name, version) } doReturn artefacts
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
        }

        val sut = ArtefactController(actionContext, mock(), repo, mock<FileSystem>(), mockConfig)

        assertThat(sut.getMetaData()).isEqualTo(artefacts)
    }

    @Test
    fun `checks report exists before getting artefacts for report`()
    {
        val artefacts = mapOf("test.png" to "hjkdasjkldas6762i1j")

        val repo = mock<ArtefactRepository> {
            on { this.getArtefactHashes(name, version) } doReturn artefacts
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
        }

        val orderly = mock<OrderlyClient>()
        val sut = ArtefactController(actionContext, orderly, repo, mock<FileSystem>(), mockConfig)
        sut.getMetaData()
        verify(orderly).checkVersionExistsForReport(name, version)
    }

    @Test
    fun `downloads artefact for report`()
    {
        val artefact = "testartefact"

        val repo = mock<ArtefactRepository> {
            on { this.getArtefactHash(name, version, artefact) } doReturn ""
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":artefact") } doReturn artefact
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }


        val fileSystem = mock<FileSystem>() {
            on { this.fileExists("root/archive/$name/$version/$artefact") } doReturn true
        }

        val sut = ArtefactController(actionContext, mock(), repo, fileSystem, mockConfig)

        sut.getFile()

        verify(actionContext).addResponseHeader("Content-Disposition", "attachment; filename=\"testname/testversion/testartefact\"")
    }

    @Test
    fun `checks report exists before downloading artefact`()
    {
        val artefact = "test.png"

        val repo = mock<ArtefactRepository> {
            on { this.getArtefactHash(name, version, artefact) } doThrow UnknownObjectError("", "")
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":artefact") } doReturn artefact
        }
        val orderly = mock<OrderlyClient>()

        val sut = ArtefactController(actionContext, orderly, repo, mock<FileSystem>(), mockConfig)
        sut.getFile()
        verify(orderly).checkVersionExistsForReport(name, version)
    }


    @Test
    fun `throws unknown object error if artefact does not exist for report`()
    {
        val artefact = "test.png"

        val repo = mock<ArtefactRepository> {
            on { this.getArtefactHash(name, version, artefact) } doThrow UnknownObjectError("", "")
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":artefact") } doReturn artefact
        }

        val sut = ArtefactController(actionContext, mock(), repo, mock<FileSystem>(), mockConfig)

        assertThatThrownBy { sut.getFile() }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `sets correct content type for csv file`()
    {
        assertCorrectContentTypeSetForFileExtension(".csv", "text/csv")
    }

    @Test
    fun `sets correct content type for png file`()
    {
        assertCorrectContentTypeSetForFileExtension(".png", "image/png")
    }

    @Test
    fun `sets correct content type for svg file`()
    {
        assertCorrectContentTypeSetForFileExtension(".svg", "image/svg+xml")
    }

    @Test
    fun `sets correct content type for pdf file`()
    {
        assertCorrectContentTypeSetForFileExtension(".pdf", "application/pdf")
    }

    @Test
    fun `sets correct content type for html file`()
    {
        assertCorrectContentTypeSetForFileExtension(".html", "text/html")
    }

    @Test
    fun `sets correct content type for css file`()
    {
        assertCorrectContentTypeSetForFileExtension(".css", "text/css")
    }

    @Test
    fun `sets correct content type for unknown file type`()
    {
        assertCorrectContentTypeSetForFileExtension(".xxx", "application/octet-stream")
    }

    private fun assertCorrectContentTypeSetForFileExtension(extension: String, expectedContentType: String)
    {
        val artefact = "testartefact$extension"

        val repo = mock<ArtefactRepository> {
            on { this.getArtefactHash(name, version, artefact) } doReturn ""
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":artefact") } doReturn artefact
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }

        val fileSystem = mock<FileSystem>() {
            on { this.fileExists("root/archive/$name/$version/$artefact") } doReturn true
        }

        val sut = ArtefactController(actionContext, mock(), repo, fileSystem, mockConfig)

        sut.getFile()

        verify(actionContext).addDefaultResponseHeaders(expectedContentType)
    }

}