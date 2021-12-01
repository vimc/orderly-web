package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.reportControllerTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
import org.vaccineimpact.orderlyweb.models.ReportVersionTags

class TagTests
{

    @Test
    fun `can tag version`()
    {
        val mockContext = mock<ActionContext> {
            on { params(":name") } doReturn "r1"
            on { params(":version") } doReturn "v1"
            on { postData<List<String>>("report_tags") } doReturn listOf("r-tag")
            on { postData<List<String>>("version_tags") } doReturn listOf("v-tag")
        }

        val mockReportRepo = mock<ReportRepository>()

        val mockTagRepo = mock<TagRepository>()
        val mockOrderly = mock<OrderlyClient>()
        val sut = ReportController(mockContext, mockOrderly, mock(), mockReportRepo, mockTagRepo)
        val result = sut.tagVersion()
        assertThat(result).isEqualTo("OK")
        verify(mockTagRepo).updateTags(eq("r1"), eq("v1"), eq(ReportVersionTags(listOf("v-tag"), listOf("r-tag"), listOf())))
        verify(mockReportRepo).getReportVersion("r1", "v1")
    }
}
