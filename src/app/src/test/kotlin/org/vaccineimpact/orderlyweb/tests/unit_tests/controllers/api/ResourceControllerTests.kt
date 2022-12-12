package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.controllers.api.ResourceController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.OrderlyFileNotFoundError
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError

class ResourceControllerTests : ControllerTest()
{
    private val mockConfig = mock<Config> {
        on { this.get("orderly.root") } doReturn "root/"
    }

    @Test
    fun `gets resources for report`()
    {
        val name = "testname"
        val version = "testversion"

        val resources = mapOf("test" to "hjkdasjkldas6762i1j")

        val orderly = mock<OrderlyClient> {
            on { this.getResourceHashes(name, version) } doReturn resources
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
        }

        val sut = ResourceController(
                actionContext,
                orderly, mock<FileSystem>(),
                mockConfig
        )

        Assertions.assertThat(sut.get()).isEqualTo(resources)
    }

    @Test
    fun `downloads resource for report`()
    {
        val name = "testname"
        val version = "testversion"
        val resource = "testresource"

        val orderly = mock<OrderlyClient> {
            on { this.getResourceHash(name, version, resource) } doReturn ""
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":resource") } doReturn resource
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }

        val fileSystem = mock<FileSystem>() {
            on { this.fileExists("root/archive/$name/$version/$resource") } doReturn true
        }

        val sut = ResourceController(actionContext, orderly, fileSystem, mockConfig)

        sut.download()

        verify(actionContext).addResponseHeader("Content-Disposition", "attachment; filename=\"testname/testversion/testresource\"")
    }

    @Test
    fun `throws unknown object error if artefact does not exist for report`()
    {
        val name = "testname"
        val version = "testversion"
        val resource = "testresource"

        val orderly = mock<OrderlyClient> {
            on { this.getResourceHash(name, version, resource) } doThrow UnknownObjectError("", "")
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":resource") } doReturn resource
        }

        val sut = ResourceController(actionContext, orderly, mock<FileSystem>(), mockConfig)

        Assertions.assertThatThrownBy { sut.download() }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `throws file not found error if file does not exist for report`()
    {
        val name = "testname"
        val version = "testversion"
        val resource = "testresource"

        val orderly = mock<OrderlyClient> {
            on { this.getResourceHash(name, version, resource) } doReturn ""
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn name
            on { this.params(":version") } doReturn version
            on { this.params(":resource") } doReturn resource
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }

        val fileSystem = mock<FileSystem>() {
            on { this.fileExists("root/archive/$name/$version/$resource") } doReturn false
        }

        val sut = ResourceController(actionContext, orderly, fileSystem, mockConfig)

        Assertions.assertThatThrownBy { sut.download() }
                .isInstanceOf(OrderlyFileNotFoundError::class.java)
    }


}