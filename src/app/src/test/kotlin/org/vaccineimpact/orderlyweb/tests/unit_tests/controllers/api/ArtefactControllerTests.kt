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
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError

class ArtefactControllerTests : ControllerTest()
{
    private val mockConfig = mock<Config> {
        on { this.get("orderly.root") } doReturn "root/"
    }

    @Test
    fun `gets artefacts for report`()
    {
        val name = "testname"
        val version = "testversion"

        val artefacts = mapOf("test.png" to "hjkdasjkldas6762i1j")

        val orderly = mock<OrderlyClient> {
            on { this.getArtefactHashes(name, version) } doReturn artefacts
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
        }

        val sut = ArtefactController(actionContext, orderly, mock<FileSystem>(), mockConfig)

        assertThat(sut.getMetaData()).isEqualTo(artefacts)
    }

    @Test
    fun `downloads artefact for report`()
    {
        val name = "testname"
        val version = "testversion"
        val artefact = "testartefact"

        val orderly = mock<OrderlyClient> {
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

        val sut = ArtefactController(actionContext, orderly, fileSystem, mockConfig)

        sut.getFile()

        verify(actionContext).addResponseHeader("Content-Disposition", "attachment; fileName=\"testname/testversion/testartefact\"")
    }

    @Test
    fun `throws unknown object error if artefact does not exist for report`()
    {
        val name = "testname"
        val version = "testversion"
        val artefact = "test.png"

        val orderly = mock<OrderlyClient> {
            on { this.getArtefactHash(name, version, artefact) } doThrow UnknownObjectError("", "")
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":artefact") } doReturn artefact
        }

        val sut = ArtefactController(actionContext, orderly, mock<FileSystem>(), mockConfig)

        assertThatThrownBy { sut.getFile() }
                .isInstanceOf(UnknownObjectError::class.java)
    }

}