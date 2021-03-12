package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController

class MetadataTests
{
    @Test
    fun `can get dependencies`()
    {
        val mockContext = mock<ActionContext>{
            on params(":name") doReturn "testName"
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mock(), mock())
    }
}
