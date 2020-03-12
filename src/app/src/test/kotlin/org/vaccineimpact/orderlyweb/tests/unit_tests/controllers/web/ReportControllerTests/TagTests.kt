package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import khttp.post
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class TagTests : TeamcityTests()
{

    @Test
    fun `can tag report`()
    {
        val mockContext = mock<ActionContext> {
            on { params(":name") } doReturn "r1"
            on { postData<List<String>>("tags") } doReturn listOf("burden-report")
        }

        val mockTagRepo = mock<TagRepository>()
        val sut = ReportController(mockContext, mock(), mockTagRepo)
        val result = sut.tagReport()
        assertThat(result).isEqualTo("OK")
        verify(mockTagRepo).tagReport("r1", listOf("burden-report"))
    }


    @Test
    fun `can tag version`()
    {
        val mockContext = mock<ActionContext> {
            on { params(":name") } doReturn "r1"
            on { params(":version") } doReturn "v1"
            on { postData<List<String>>("tags") } doReturn listOf("burden-report")
        }

        val mockTagRepo = mock<TagRepository>()
        val mockOrderly = mock<OrderlyClient>()
        val sut = ReportController(mockContext, mockOrderly, mockTagRepo)
        val result = sut.tagVersion()
        assertThat(result).isEqualTo("OK")
        verify(mockTagRepo).tagVersion("v1", listOf("burden-report"))
        verify(mockOrderly).checkVersionExistsForReport("r1", "v1")
    }
}