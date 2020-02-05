package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.After
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.Files
import org.vaccineimpact.orderlyweb.controllers.web.DocumentController
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest
import java.io.File

class DocumentControllerTests : ControllerTest()
{
    @After
    fun cleanup() {
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

        val sut = DocumentController(mockContext, AppConfig(), Files())
        sut.getDocument()
        verify(mockContext)
                .addResponseHeader("Content-Disposition", "attachment; filename=\"some/path/file.csv\"")
        verify(mockContext).addDefaultResponseHeaders("text/csv")
    }
}